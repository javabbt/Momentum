package com.yannick.featureauth.presentation.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.gif.GifDecoder
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Size
import com.yannick.resources.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onDarkModeToggle: () -> Unit,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier,
    onContinueClick: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
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
            val context = LocalContext.current
            Text(
                text = stringResource(id = R.string.welcome_message),
                fontSize = 24.sp,
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(modifier = Modifier.height(6.dp))
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(R.drawable.selfie) // Reference to your local GIF
                    .decoderFactory(GifDecoder.Factory()) // Enables GIF rendering
                    .size(Size.ORIGINAL) // Keeps original resolution
                    .crossfade(true)
                    .build(),
                contentDescription = "Local GIF",
                modifier = modifier
                    .size(150.dp),
                contentScale = ContentScale.Crop,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(id = R.string.join_friends_message),
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onContinueClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(id = R.string.continue_button))
            }
        }
    }
}

@Composable
fun OnboardingContainer(
    onDarkModeToggle: () -> Unit,
    isDarkMode: Boolean,
    onContinueClick: () -> Unit = {},
) {
    OnboardingScreen(
        onDarkModeToggle = onDarkModeToggle,
        isDarkMode = isDarkMode,
        onContinueClick = onContinueClick,
    )
}
