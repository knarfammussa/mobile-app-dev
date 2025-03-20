package com.zybooks.quickdraw.ui.game

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zybooks.quickdraw.model.Category
import com.zybooks.quickdraw.model.GameSettings
import com.zybooks.quickdraw.model.Player
import com.zybooks.quickdraw.model.SampleCategories
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _timeLeft = MutableStateFlow(15)
    val timeLeft: StateFlow<Int> = _timeLeft.asStateFlow()

    private val _isSpeechRecognitionActive = MutableStateFlow(false)
    val isSpeechRecognitionActive: StateFlow<Boolean> = _isSpeechRecognitionActive.asStateFlow()

    private val usedWords = mutableSetOf<String>()
    private var timerJob: Job? = null

    // speech recognition
    private var speechRecognizer: SpeechRecognizer? = null
    private val speechRecognizerIntent by lazy {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
    }

    fun setupNewGame(playerNames: List<String>, selectedCategoryName: String? = null) {
        // create players from names (names kind of obsolete but thats okay)
        val players = playerNames.map { name -> Player(name = name) }

        // use selected category or pick random one (didn't implement a set-up screen for selecting one yet)
        val category = if (selectedCategoryName != null) {
            SampleCategories.categories.find { it.name == selectedCategoryName }
                ?: SampleCategories.getRandomCategory()
        } else {
            SampleCategories.getRandomCategory()
        }

        // reset game state
        usedWords.clear()

        // set up the first player as active
        val playersWithActive = players.toMutableList()
        if (playersWithActive.isNotEmpty()) {
            playersWithActive[0] = playersWithActive[0].copy(isActive = true)
        }

        // val gameSettings = GameSettings()
        val preferences = getApplication<Application>().getSharedPreferences("quickdraw_settings", Context.MODE_PRIVATE)
        val timerDuration = preferences.getInt("timer_duration", 15)
        val minPlayers = preferences.getInt("min_players", 2)
        val maxPlayers = preferences.getInt("max_players", 8)
        val soundEffects = preferences.getBoolean("sound_effects", true)
        val vibration = preferences.getBoolean("vibration", true)

        val gameSettings = GameSettings(
            timerDurationSeconds = timerDuration,
            minPlayers = minPlayers,
            maxPlayers = maxPlayers,
            enableSoundEffects = soundEffects,
            enableVibration = vibration
        )

        // apply settings
        _uiState.value = GameUiState(
            gameState = GameState.READY,
            players = playersWithActive,
            currentPlayerIndex = 0,
            category = category,
            settings = gameSettings
        )
    }

    fun startGame() {
        _uiState.value = _uiState.value.copy(gameState = GameState.PLAYING)
        startTimer()
    }

    fun pauseGame() {
        _uiState.value = _uiState.value.copy(
            gameState = GameState.PAUSED,
            pausedTimeRemaining = _timeLeft.value // curr time remaining
        )
        timerJob?.cancel()
    }

    fun resumeGame() {
        // restore paused time (NEED TO FIX)
        val timeToResume = _uiState.value.pausedTimeRemaining ?: 15
        _timeLeft.value = timeToResume

        _uiState.value = _uiState.value.copy(
            gameState = GameState.PLAYING,
            pausedTimeRemaining = null
        )
        startTimer()
    }

    fun submitWord(word: String): Boolean {
        // skip if game isn't in playing state
        if (_uiState.value.gameState != GameState.PLAYING) return false

        // validate the word (check not empty and hasn't been used)
        if (word.isBlank() || usedWords.contains(word.trim().lowercase())) {
            return false
        }

        // validation check
        val isValid = isWordValidForCategory(word)

        if (isValid) {
            usedWords.add(word.trim().lowercase())

            // update player score
            val updatedPlayers = _uiState.value.players.toMutableList()
            val currentPlayerIndex = _uiState.value.currentPlayerIndex
            val currentPlayer = updatedPlayers[currentPlayerIndex]
            updatedPlayers[currentPlayerIndex] = currentPlayer.copy(
                score = currentPlayer.score + 1
            )

            // move to next player
            val nextPlayerIndex = (currentPlayerIndex + 1) % updatedPlayers.size

            // update active player
            updatedPlayers.forEachIndexed { index, player ->
                updatedPlayers[index] = player.copy(isActive = index == nextPlayerIndex)
            }

            _uiState.value = _uiState.value.copy(
                players = updatedPlayers,
                currentPlayerIndex = nextPlayerIndex,
                lastEnteredWord = word
            )

            // reset timer
            resetTimer()
        }

        return isValid
    }

    fun timeOut() {
        // current player loses
        val currentPlayer = _uiState.value.players[_uiState.value.currentPlayerIndex]

        _uiState.value = _uiState.value.copy(
            gameState = GameState.FINISHED,
            loser = currentPlayer
        )

        timerJob?.cancel()
    }

    fun exitGame() {
        _uiState.value = _uiState.value.copy(
            gameState = GameState.EXIT,
            navigateToHome = true
        )
    }

    fun onNavigationHandled() {
        _uiState.value = _uiState.value.copy(navigateToHome = false)
    }

    fun startSpeechRecognition() {
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplication())
            setupSpeechRecognizer()
        }

        speechRecognizer?.startListening(speechRecognizerIntent)
        _isSpeechRecognitionActive.value = true
    }

    fun stopSpeechRecognition() {
        speechRecognizer?.stopListening()
        _isSpeechRecognitionActive.value = false
    }

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    private fun setupSpeechRecognizer() {
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _recognizedText.value = "Listening..."
            }

            override fun onBeginningOfSpeech() {
                _recognizedText.value = "Listening..."
            }

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                _recognizedText.value = "Processing..."
                _isSpeechRecognitionActive.value = false
            }

            override fun onError(error: Int) {
                _isSpeechRecognitionActive.value = false
                _recognizedText.value = when(error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No match found"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
                    SpeechRecognizer.ERROR_SERVER -> "Server error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                    else -> "Unknown error"
                }

                // clear error message after delay
                viewModelScope.launch {
                    delay(3000)
                    _recognizedText.value = ""
                }
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val word = matches[0]
                    _recognizedText.value = "\"$word\""

                    val isValid = submitWord(word)
                    if (!isValid) {
                        _recognizedText.value = "\"$word\" - Not valid for this category"
                        // clear message after delay
                        viewModelScope.launch {
                            delay(2000)
                            _recognizedText.value = ""
                        }
                    }
                }
                _isSpeechRecognitionActive.value = false
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    _recognizedText.value = "Heard: ${matches[0]}"
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    private fun startTimer() {
        timerJob?.cancel()

        timerJob = viewModelScope.launch {
            // timer duration from settings
            val timerDuration = _uiState.value.pausedTimeRemaining ?: _uiState.value.settings.timerDurationSeconds

            // start with full timer
            _timeLeft.value = timerDuration

            // count down every sec
            while (_timeLeft.value > 0) {
                delay(1000)
                _timeLeft.value = _timeLeft.value - 1
            }

            // current player loses
            if (_uiState.value.gameState == GameState.PLAYING) {
                timeOut()
            }
        }
    }

    private fun resetTimer() {
        timerJob?.cancel()
        _timeLeft.value = 15

        if (_uiState.value.gameState == GameState.PLAYING) {
            startTimer()
        }
    }

    private fun isWordValidForCategory(word: String): Boolean {
        // skip empty words
        if (word.isBlank()) return false

        val category = _uiState.value.category
        val formattedWord = word.trim().lowercase()

        // check if word is in the list (caps don't matterrrr!)
        return category.words.any { it.lowercase() == formattedWord }
    }
}

data class GameUiState(
    val gameState: GameState = GameState.SETUP,
    val players: List<Player> = emptyList(),
    val currentPlayerIndex: Int = 0,
    val category: Category = Category(name = ""),
    val lastEnteredWord: String = "",
    val loser: Player? = null,
    val navigateToHome: Boolean = false,
    val microphoneError: String? = null,
    val pausedTimeRemaining: Int? = null,
    val settings: GameSettings = GameSettings()
)

enum class GameState {
    SETUP,     // game setup (for choosing players/category)
    READY,     // ready to start
    PLAYING,   // game in progress
    PAUSED,    // game paused
    FINISHED,  // game over
    EXIT       // exit game
}