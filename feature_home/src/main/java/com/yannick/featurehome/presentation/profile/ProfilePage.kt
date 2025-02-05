package com.yannick.featurehome.presentation.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.yannick.core.theme.Red
import com.yannick.resources.R

val uri = "https://www.momentum.com"

@Composable
fun ProfilePage(
    modifier: Modifier = Modifier,
    profileUiState: ProfileUiState,
    onLogout: () -> Unit,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .scrollable(
                scrollState,
                orientation = Orientation.Vertical,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Profile Image
        AsyncImage(
            model = profileUiState.profilePicture ?: R.drawable.profile,
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color.LightGray),
            contentScale = ContentScale.Crop,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Name
        Text(
            text = profileUiState.displayName,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )

        // Username with copy icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp),
        ) {
            Text(
                text = "@${profileUiState.shortUserName}",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
            )
            
        }

        // Stats Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            StatItem(
                count = profileUiState.chains.toString(),
                label = stringResource(R.string.chains),
            )
            StatItem(
                count = profileUiState.friends.toString(),
                label = stringResource(R.string.friends),
            )
            StatItem(
                count = profileUiState.streaks.toString(),
                label = stringResource(R.string.streaks),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Manage Friends Button
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Red),
            border = BorderStroke(1.dp, Red),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.logout),
                contentDescription = null,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(24.dp),
                tint = Red,
            )
            Text(stringResource(R.string.logout), style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun StatItem(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
        )
    }
}
