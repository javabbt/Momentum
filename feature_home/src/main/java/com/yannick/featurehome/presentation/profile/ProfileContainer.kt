package com.yannick.featurehome.presentation.profile

import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.yannick.core.utils.OnboardingScreen
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileContainer(modifier: Modifier = Modifier, navController: NavController) {
    val viewmodel: ProfileViewModel = koinViewModel()
    val uiState by viewmodel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    stringResource(com.yannick.resources.R.string.logout),
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            text = {
                Text(
                    stringResource(com.yannick.resources.R.string.logout_message),
                    style = MaterialTheme.typography.labelMedium,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    viewmodel.logout()
                }) {
                    Text(
                        stringResource(com.yannick.resources.R.string.yes),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(
                        stringResource(com.yannick.resources.R.string.no),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            },
        )
    }

    LaunchedEffect(viewmodel) {
        viewmodel.sideEffects.onEach {
            when (it) {
                is SideEffect.ShowError -> {
                    Toast.makeText(context, it.msg, Toast.LENGTH_SHORT).show()
                }

                is SideEffect.ShowUnexpectedError -> {
                    Toast.makeText(context, it.msg, Toast.LENGTH_SHORT).show()
                }

                is SideEffect.LogoutDone -> {
                    navController.navigate(OnboardingScreen) {
                        popUpTo(OnboardingScreen) {
                            inclusive = true
                        }
                    }
                }
            }
        }.collect()
    }

    ProfilePage(
        profileUiState = uiState,
        modifier = modifier,
        onLogout = { showLogoutDialog = true },
    )
}
