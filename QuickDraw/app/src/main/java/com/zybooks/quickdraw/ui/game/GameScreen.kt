package com.zybooks.quickdraw.ui.game

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import com.zybooks.quickdraw.model.Player
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

// pass timer max value as a parameter to GamePlayingScreen
// maybe separate diff game screens
@Composable
fun GameScreen(
    onNavigateToHome: () -> Unit
) {
    // use Application context for speech rec
    val context = androidx.compose.ui.platform.LocalContext.current
    val application = context.applicationContext as android.app.Application
    val viewModel: GameViewModel = viewModel(factory = GameViewModelFactory(application))

    val uiState by viewModel.uiState.collectAsState()
    val timeLeft by viewModel.timeLeft.collectAsState()
    val isSpeechRecognitionActive by viewModel.isSpeechRecognitionActive.collectAsState()
    val recognizedText by viewModel.recognizedText.collectAsState()

    // get timer max value from settings
    val timerMaxValue = uiState.settings.timerDurationSeconds

    // handle nav
    if (uiState.navigateToHome) {
        onNavigateToHome()
        viewModel.onNavigationHandled()
    }

    // setup a new game if in SETUP state (no logic for setup screen yet)
    LaunchedEffect(key1 = true) {
        if (uiState.gameState == GameState.SETUP) {
            // default to 2 players and random category
            viewModel.setupNewGame(listOf("Player 1", "Player 2"))
        }
    }

    // request microphone perms
    var hasRequestedPermission by remember { mutableStateOf(false) }

    if (uiState.gameState == GameState.PLAYING && !hasRequestedPermission) {
        com.zybooks.quickdraw.ui.permissions.MicrophonePermissionHandler(
            onPermissionGranted = {
                hasRequestedPermission = true
            }
        )
    }

    // show screen based on game state
    when (uiState.gameState) {
        GameState.SETUP -> {
            // loading if in setup
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
        GameState.READY -> {
            GameReadyScreen(
                category = uiState.category.name,
                players = uiState.players,
                onStartGame = viewModel::startGame,
                onExitGame = viewModel::exitGame
            )
        }
        GameState.PLAYING -> {
            GamePlayingScreen(
                category = uiState.category.name,
                players = uiState.players,
                currentPlayerIndex = uiState.currentPlayerIndex,
                timeLeft = timeLeft,
                lastEnteredWord = uiState.lastEnteredWord,
                onSubmitWord = viewModel::submitWord,
                onPauseGame = viewModel::pauseGame,
                isSpeechRecognitionActive = isSpeechRecognitionActive,
                onStartSpeechRecognition = viewModel::startSpeechRecognition,
                onStopSpeechRecognition = viewModel::stopSpeechRecognition,
                recognizedText = recognizedText,
                timerMaxValue = timerMaxValue
            )
        }
        GameState.PAUSED -> {
            GamePausedScreen(
                onResumeGame = viewModel::resumeGame,
                onExitGame = viewModel::exitGame
            )
        }
        GameState.FINISHED -> {
            GameFinishedScreen(
                loserName = uiState.loser?.name ?: "Unknown",
                onPlayAgain = {
                    // start new game with same players and new random category
                    val playerNames = uiState.players.map { it.name }
                    viewModel.setupNewGame(playerNames)
                },
                onExitGame = viewModel::exitGame
            )
        }
        GameState.EXIT -> {
            // navigate back which is handled above in onExitGame
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameReadyScreen(
    category: String,
    players: List<Player>,
    onStartGame: () -> Unit,
    onExitGame: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // top app bar (says it is experimental so hopefully will stay working!)
        TopAppBar(
            title = { Text("Quick Draw") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )

        // category and player info
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Category",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = category,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Text(
                text = "Players",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            players.forEach { player ->
                Text(
                    text = player.name,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }

        // start and exit buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = onExitGame,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text("Exit")
            }

            Button(
                onClick = onStartGame,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text("Start Game")
            }
        }
    }
}

@Composable
fun GamePlayingScreen(
    category: String,
    players: List<Player>,
    currentPlayerIndex: Int,
    timeLeft: Int,
    lastEnteredWord: String,
    onSubmitWord: (String) -> Boolean,
    onPauseGame: () -> Unit,
    isSpeechRecognitionActive: Boolean = false,
    onStartSpeechRecognition: () -> Unit = {},
    onStopSpeechRecognition: () -> Unit = {},
    recognizedText: String = "",
    timerMaxValue: Int = 15
) {
    val currentPlayer = if (players.isNotEmpty() && currentPlayerIndex < players.size) {
        players[currentPlayerIndex]
    } else {
        Player(name = "Player")
    }

    var wordInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    // calc progress for timer
    val progress by animateFloatAsState(
        targetValue = timeLeft.toFloat() / timerMaxValue.toFloat(),
        label = "timer"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // top bar w/ category and pause button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Category: $category",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = onPauseGame) {
                Icon(
                    imageVector = Icons.Filled.Pause,
                    contentDescription = "Pause Game"
                )
            }
        }

        // timer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                color = when {
                    timeLeft > 10 -> Color.Green
                    timeLeft > 5 -> Color.Yellow
                    else -> Color.Red
                }
            )

            Text(
                text = "$timeLeft seconds",
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 20.dp)
            )
        }

        // curr player
        Text(
            text = "${currentPlayer.name}'s Turn",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // speech input button
        Button(
            onClick = {
                if (isSpeechRecognitionActive) {
                    onStopSpeechRecognition()
                } else {
                    onStartSpeechRecognition()
                }
            },
            modifier = Modifier
                .size(120.dp)
                .padding(vertical = 8.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isSpeechRecognitionActive)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Microphone",
                    modifier = Modifier.size(36.dp)
                )
                Text(
                    text = if (isSpeechRecognitionActive) "Stop" else "Speak",
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        // speech recognition status
        if (recognizedText.isNotEmpty()) {
            Text(
                text = recognizedText,
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }

        // manual word input for testing/backup if speech goes bad
        OutlinedTextField(
            value = wordInput,
            onValueChange = {
                wordInput = it
                errorMessage = ""
            },
            label = { Text("Or type a word for $category") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    val success = onSubmitWord(wordInput)
                    if (success) {
                        wordInput = ""
                    } else {
                        errorMessage = "Invalid word or already used"
                    }
                }
            )
        )

        // submit button for typed words
        Button(
            onClick = {
                val success = onSubmitWord(wordInput)
                if (success) {
                    wordInput = ""
                } else {
                    errorMessage = "Invalid word or already used"
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Submit Typed Word")
        }

        // error message
        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // last word entered
        if (lastEnteredWord.isNotEmpty()) {
            Text(
                text = "Last word: $lastEnteredWord",
                fontSize = 16.sp,
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // player scores
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 16.dp)
        ) {
            Text(
                text = "Scores:",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            players.forEach { player ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (player.isActive) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .padding(end = 8.dp)
                            )
                        }

                        Text(
                            text = player.name,
                            fontWeight = if (player.isActive) FontWeight.Bold else FontWeight.Normal
                        )
                    }

                    Text(
                        text = "${player.score}",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun GamePausedScreen(
    onResumeGame: () -> Unit,
    onExitGame: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Game Paused",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = onResumeGame,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .padding(vertical = 8.dp)
        ) {
            Text("Resume Game")
        }

        Button(
            onClick = onExitGame,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            ),
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .padding(vertical = 8.dp)
        ) {
            Text("Exit Game")
        }
    }
}

@Composable
fun GameFinishedScreen(
    loserName: String,
    onPlayAgain: () -> Unit,
    onExitGame: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Game Over!",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "$loserName ran out of time!",
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = onPlayAgain,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .padding(vertical = 8.dp)
        ) {
            Text("Play Again")
        }

        Button(
            onClick = onExitGame,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            ),
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .padding(vertical = 8.dp)
        ) {
            Text("Back to Home")
        }
    }
}