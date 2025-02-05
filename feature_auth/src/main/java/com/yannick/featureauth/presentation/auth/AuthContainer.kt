package com.yannick.featureauth.presentation.auth

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialProviderConfigurationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.navigation.NavHostController
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.yannick.core.utils.HomeScreen
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

@Composable
fun AuthContainer(
    onDarkModeToggle: () -> Unit,
    isDarkMode: Boolean,
    navController: NavHostController,
) {
    val viewModel: AuthViewModel = koinViewModel()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(viewModel) {
        viewModel.sideEffects.onEach { sideEffect ->
            when (sideEffect) {
                is SideEffect.ShowError -> {
                    Toast.makeText(
                        context,
                        sideEffect.msg,
                        Toast.LENGTH_SHORT,
                    ).show()
                }

                is SideEffect.ShowUnexpectedError -> {
                    Toast.makeText(
                        context,
                        sideEffect.msg,
                        Toast.LENGTH_SHORT,
                    ).show()
                }

                is SideEffect.LoginDone -> {
                    navController.navigate(HomeScreen) {
                        popUpTo(HomeScreen) {
                            inclusive = true
                        }
                    }
                }
            }
        }.collect()
    }
    AuthScreen(isDarkMode = isDarkMode, onDarkModeToggle = onDarkModeToggle, onLoginClick = {
        coroutineScope.launch {
            launchCredManButtonUI(context) { credential ->
                viewModel.signInWithGoogle(credential)
            }
        }
    })
}

private suspend fun launchCredManButtonUI(
    context: Context,
    onRequestResult: (Credential) -> Unit,
) {
    try {
        val signInWithGoogleOption = GetSignInWithGoogleOption
            .Builder(serverClientId = context.getString(com.yannick.resources.R.string.default_web_client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        val result = CredentialManager.create(context).getCredential(
            request = request,
            context = context,
        )

        onRequestResult(result.credential)
    } catch (e: NoCredentialException) {
        Timber.e(e)
    } catch (e: GetCredentialException) {
        Timber.e(e)
    } catch (e: GetCredentialProviderConfigurationException) {
        Timber.e(e)
    }
}
