package com.yannick.featurehome.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.google.firebase.auth.FirebaseUser
import com.yannick.core.theme.Blue
import com.yannick.core.utils.CreateChainScreen
import com.yannick.featurehome.presentation.chains.ChainContainer
import com.yannick.featurehome.presentation.friends.FriendsContainer
import com.yannick.featurehome.presentation.profile.ProfileContainer
import com.yannick.resources.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(
    onDarkModeToggle: () -> Unit,
    isDarkMode: Boolean = false,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit = {},
    user: FirebaseUser?,
    navController: NavController,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(id = R.string.momentum))
                },
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
                    IconButton(onClick = {}) {
                        AsyncImage(
                            model = user?.photoUrl ?: R.drawable.profile,
                            contentDescription = "Profile picture",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate(CreateChainScreen)
            }) {
                Image(
                    painter = painterResource(id = R.drawable.camera),
                    contentDescription = "Add",
                    colorFilter = if (isDarkMode) ColorFilter.tint(Color.White) else null,
                    modifier = Modifier.size(24.dp),
                )
            }
        },
        bottomBar = {
            BottomAppBar(modifier = Modifier.fillMaxWidth(), actions = {
                Row {
                    IconButton(onClick = {
                        onTabSelected(0)
                    }, modifier = Modifier.weight(1f)) {
                        Image(
                            painter = painterResource(id = R.drawable.home),
                            contentDescription = "Home",
                            modifier = Modifier.size(24.dp),
                            colorFilter = if (selectedIndex == 0) {
                                ColorFilter.tint(Blue)
                            } else if (isDarkMode) {
                                ColorFilter.tint(
                                    Color.White,
                                )
                            } else {
                                null
                            },
                        )
                    }
                    IconButton(onClick = {
                        onTabSelected(1)
                    }, modifier = Modifier.weight(1f)) {
                        Image(
                            painter = painterResource(id = R.drawable.friends),
                            contentDescription = "Friends",
                            modifier = Modifier.size(24.dp),
                            colorFilter = if (selectedIndex == 1) {
                                ColorFilter.tint(Blue)
                            } else if (isDarkMode) {
                                ColorFilter.tint(
                                    Color.White,
                                )
                            } else {
                                null
                            },
                        )
                    }
                    IconButton(onClick = {
                        onTabSelected(2)
                    }, modifier = Modifier.weight(1f)) {
                        Image(
                            painter = painterResource(id = R.drawable.settings),
                            contentDescription = "Settings",
                            modifier = Modifier.size(24.dp),
                            colorFilter = if (selectedIndex == 2) {
                                ColorFilter.tint(Blue)
                            } else if (isDarkMode) {
                                ColorFilter.tint(
                                    Color.White,
                                )
                            } else {
                                null
                            },
                        )
                    }
                }
            })
        },
    ) { innerPadding ->
        when (selectedIndex) {
            0 -> ChainContainer(
                modifier = Modifier.padding(innerPadding),
                navController = navController,
            )

            1 -> FriendsContainer(
                modifier = Modifier.padding(innerPadding),
                navController = navController,
            )

            2 -> ProfileContainer(
                modifier = Modifier.padding(innerPadding),
                navController = navController,
            )
        }
    }
}
