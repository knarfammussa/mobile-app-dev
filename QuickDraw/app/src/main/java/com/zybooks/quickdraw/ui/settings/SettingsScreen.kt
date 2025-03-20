package com.zybooks.quickdraw.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(context.applicationContext as android.app.Application)
    )

    val uiState by viewModel.uiState.collectAsState()

    // handle nav!
    if (uiState.navigateBack) {
        onNavigateBack()
        viewModel.onNavigationHandled()
    }

    // success!
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.settingsSaved) {
        if (uiState.settingsSaved) {
            snackbarHostState.showSnackbar("Settings saved successfully")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 16.dp)
                        )
                    } else {
                        IconButton(onClick = viewModel::saveSettings) {
                            Icon(Icons.Default.Check, contentDescription = "Save")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Game Timer Setting
            SettingSection(title = "Game Timer") {
                val timerValue = uiState.settings.timerDurationSeconds

                Text(
                    text = "Time per turn: $timerValue seconds",
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Slider(
                    value = timerValue.toFloat(),
                    onValueChange = { viewModel.updateTimerDuration(it.toInt()) },
                    valueRange = 5f..60f,
                    steps = 11, // (60-5)/5 = 11 steps
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("5s")
                    Text("30s")
                    Text("60s")
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // player settings
            SettingSection(title = "Players") {
                val minPlayers = uiState.settings.minPlayers
                val maxPlayers = uiState.settings.maxPlayers

                Text(
                    text = "Minimum players: $minPlayers",
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Slider(
                    value = minPlayers.toFloat(),
                    onValueChange = { viewModel.updateMinPlayers(it.toInt()) },
                    valueRange = 2f..4f,
                    steps = 2, // 2,3,4 = 3 values, so 2 steps
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Maximum players: $maxPlayers",
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Slider(
                    value = maxPlayers.toFloat(),
                    onValueChange = { viewModel.updateMaxPlayers(it.toInt()) },
                    valueRange = minPlayers.toFloat()..8f,
                    steps = 8 - minPlayers,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // sound and vibration (not really included right now but we have the UI at least!)
            SettingSection(title = "Sound & Feedback") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Sound Effects")
                    Switch(
                        checked = uiState.settings.enableSoundEffects,
                        onCheckedChange = { viewModel.toggleSoundEffects() }
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Vibration")
                    Switch(
                        checked = uiState.settings.enableVibration,
                        onCheckedChange = { viewModel.toggleVibration() }
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // reset to defaults button
            Button(
                onClick = viewModel::resetSettings,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 16.dp)
            ) {
                Text("Reset to Defaults")
            }
        }
    }
}

@Composable
fun SettingSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        content()
    }
}