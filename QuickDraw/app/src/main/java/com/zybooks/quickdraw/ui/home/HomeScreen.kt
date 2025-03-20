package com.zybooks.quickdraw.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HomeScreen(
    onNavigateToGameSetup: () -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // navigatiooooon
    if (uiState.navigateToGameSetup) {
        onNavigateToGameSetup()
        viewModel.onNavigationHandled()
    }

    if (uiState.navigateToLeaderboard) {
        onNavigateToLeaderboard()
        viewModel.onNavigationHandled()
    }

    if (uiState.navigateToSettings) {
        onNavigateToSettings()
        viewModel.onNavigationHandled()
    }

    HomeContent(
        onStartGameClicked = viewModel::onStartGameClicked,
        onLeaderboardClicked = viewModel::onLeaderboardClicked,
        onSettingsClicked = viewModel::onSettingsClicked
    )
}

@Composable
fun HomeContent(
    onStartGameClicked: () -> Unit,
    onLeaderboardClicked: () -> Unit,
    onSettingsClicked: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // top app bar
            HomeAppBar()

            // main content
            HomeButtons(
                onStartGameClicked = onStartGameClicked,
                onLeaderboardClicked = onLeaderboardClicked
            )

            // bottom alignment
            Spacer(modifier = Modifier.weight(1f))
        }

        // settings button at bottom right
        IconButton(
            onClick = onSettingsClicked,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun HomeAppBar() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Quick Draw",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Word Association Game",
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun HomeButtons(
    onStartGameClicked: () -> Unit,
    onLeaderboardClicked: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Button(
            onClick = onStartGameClicked,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(64.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "Start Game",
                fontSize = 20.sp
            )
        }

        Button(
            onClick = onLeaderboardClicked,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(64.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text(
                text = "Leaderboard",
                fontSize = 20.sp
            )
        }
    }
}