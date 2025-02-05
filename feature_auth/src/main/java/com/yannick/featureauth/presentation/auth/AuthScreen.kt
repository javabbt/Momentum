package com.yannick.featureauth.presentation.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yannick.resources.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    modifier: Modifier = Modifier,
    onDarkModeToggle: () -> Unit,
    onLoginClick: () -> Unit = {},
    isDarkMode: Boolean = false,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                actions = {
                    IconButton(onClick = {
                        onDarkModeToggle()
                    }) {
                        Image(
                            painter = painterResource(id = R.drawable.night),
                            contentDescription = "Toggle dark mode",
                            colorFilter = ColorFilter.tint(
                                if (isDarkMode) Color.White else Color.Black,
                            ),
                            modifier = Modifier.size(28.dp),
                        )
                    }
                },
            )
        },
    ) { _ ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(id = R.string.login),
                fontSize = 24.sp,
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(id = R.string.login_message),
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    onLoginClick()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row {
                    Image(
                        painter = painterResource(id = R.drawable.google),
                        contentDescription = "Google",
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(Color.White),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.google_sign_in_button),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(id = R.string.terms_privacy_message),
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                fontSize = 11.sp,
            )
        }
    }
}
