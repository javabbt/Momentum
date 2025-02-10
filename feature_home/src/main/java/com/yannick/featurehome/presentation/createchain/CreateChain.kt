package com.yannick.featurehome.presentation.createchain

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.yannick.resources.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChain(
    navController: NavController,
    onImageSelected: (String) -> Unit,
    uriFile: Uri?,
    onSubmit: (String, String, Uri?) -> Unit,
    loading: Boolean = false,
) {
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri?.let {
            onImageSelected(it.toString())
        }
    }
    var theme by remember { mutableStateOf("") }
    var caption by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.new_chain)) },
            navigationIcon = {
                IconButton(onClick = {
                    navController.popBackStack()
                }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                    )
                }
            },
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            // Image placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable {
                        galleryLauncher.launch("image/*")
                    }
                    .background(Color.LightGray, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                if (uriFile != null) {
                    AsyncImage(
                        model = uriFile,
                        contentDescription = stringResource(R.string.selected_image),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    )
                } else {
                    IconButton(onClick = { galleryLauncher.launch("image/*") }) {
                        Icon(
                            painter = painterResource(R.drawable.camera),
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (loading) {
                CircularProgressIndicator()
            }

            // Theme input
            OutlinedTextField(
                value = theme,
                onValueChange = {
                    theme = it
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.add_theme_hint)) },
                leadingIcon = { Text("#", style = MaterialTheme.typography.bodyLarge) },
            )
            Spacer(modifier = Modifier.height(16.dp))
            // Caption input
            OutlinedTextField(
                value = caption,
                onValueChange = {
                    caption = it
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.write_caption_hint)) },
                leadingIcon = { Icon(Icons.Default.Face, contentDescription = null) },
            )

            Spacer(modifier = Modifier.weight(1f))

            // Next button
            Button(
                onClick = {
                    onSubmit(theme, caption, uriFile)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                enabled = loading.not(),
            ) {
                Text(stringResource(R.string.next))
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
}
