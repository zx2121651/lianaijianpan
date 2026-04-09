package com.lovekey.ime

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.lovekey.ime.ui.KeyboardMode

class InputSessionController(
    private val engineAdapter: CandidateEngineAdapter,
    private val commitPolicy: CommitPolicy,
    private val onAffectionChange: (Int) -> Unit = {}
) {
    // A scope bound to the lifetime of the IMEService, or cancelled externally
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var searchJob: Job? = null

    private val _rawInput = MutableStateFlow("")
    val rawInput: StateFlow<String> = _rawInput.asStateFlow()

    private val _displayPinyinText = MutableStateFlow("")
    val displayPinyinText: StateFlow<String> = _displayPinyinText.asStateFlow()

    private val _candidateList = MutableStateFlow<List<String>>(emptyList())
    val candidateList: StateFlow<List<String>> = _candidateList.asStateFlow()

    private val _t9PinyinCombinations = MutableStateFlow<List<String>>(emptyList())
    val t9PinyinCombinations: StateFlow<List<String>> = _t9PinyinCombinations.asStateFlow()

    private val _isEnglishMode = MutableStateFlow(false)
    val isEnglishMode: StateFlow<Boolean> = _isEnglishMode.asStateFlow()

    private val _currentMode = MutableStateFlow(KeyboardMode.QWERTY)
    val currentMode: StateFlow<KeyboardMode> = _currentMode.asStateFlow()

    private val _previousMode = MutableStateFlow(KeyboardMode.QWERTY)
    val previousMode: StateFlow<KeyboardMode> = _previousMode.asStateFlow()


    private var isT9Mode = false

    fun handleKeyPress(key: String) {
        when (key) {
            ",", ".", "?", "!" -> {
                commitPolicy.commitPunctuation(
                    key = key,
                    isEnglishMode = _isEnglishMode.value,
                    hasCandidates = _candidateList.value.isNotEmpty(),
                    firstCandidate = _candidateList.value.firstOrNull(),
                    rawInput = _rawInput.value
                )
                clearSession()
            }
            "1" -> {
                if (_isEnglishMode.value) {
                    commitPolicy.commitText("1")
                } else {
                    if (_rawInput.value.isEmpty()) {
                        commitPolicy.commitText("，")
                    } else {
                        updateRawInput(_rawInput.value + "1")
                    }
                }
            }
            "CLEAR" -> {
                clearSession()
            }
            "DEL" -> {
                if (_rawInput.value.isNotEmpty()) {
                    updateRawInput(_rawInput.value.dropLast(1))
                } else {
                    commitPolicy.deleteSurroundingText(1, 0)
                }
            }
            "SPACE" -> {
                if (_candidateList.value.isNotEmpty()) {
                    commitPolicy.commitText(_candidateList.value.first())
                    clearSession()
                } else {
                    commitPolicy.commitText(" ")
                }
            }
            "ENT" -> {
                if (_displayPinyinText.value.isNotEmpty()) {
                    commitPolicy.commitText(_displayPinyinText.value)
                    clearSession()
                } else {
                    commitPolicy.commitNewlineOrAction()
                }
            }
            "中/英" -> {
                // Handled partially in Compose UI, but if reached here, we can toggle or ignore
            }
            else -> {
                val isLetterOrT9Digit = key.length == 1 && (key[0].isLetter() || (key[0].isDigit() && key[0] != '0' && key[0] != '1'))
                if (_isEnglishMode.value || !isLetterOrT9Digit) {
                    if (!isLetterOrT9Digit && !_isEnglishMode.value) {
                        if (_candidateList.value.isNotEmpty()) {
                            commitPolicy.commitText(_candidateList.value.first())
                        } else if (_rawInput.value.isNotEmpty()) {
                            commitPolicy.commitText(_rawInput.value)
                        }
                        clearSession()
                    }
                    commitPolicy.commitText(key)
                } else {
                    val isDigit = key.length == 1 && key[0].isDigit() && key != "0" && key != "1"
                    if (_rawInput.value.isEmpty()) {
                        isT9Mode = isDigit
                    }
                    updateRawInput(_rawInput.value + key.lowercase())
                }
            }
        }
    }

    fun handleCandidateSelected(candidate: String) {
        val currentPinyin = _displayPinyinText.value

        commitPolicy.commitText(candidate)
        clearSession()

        // Asynchronously update frequency memory
        if (currentPinyin.isNotEmpty()) {
            scope.launch {
                engineAdapter.chooseCandidate(candidate)
            }
        }

        onAffectionChange(2) // Positive reinforcement for using its suggestion
    }

    fun handleSyllableSelected(syllable: String) {
        _displayPinyinText.value = syllable
        // Cancel any pending search jobs
        searchJob?.cancel()
        searchJob = scope.launch {
            // When user explicitly selects a different T9 permutation or syllable from the bar,
            // we do a full Pinyin search for that specific string to populate candidates.
            val (_, candidates, _) = engineAdapter.searchPinyin(syllable, isT9 = false)
            _candidateList.value = candidates
        }
    }


    fun setKeyboardMode(mode: KeyboardMode) {
        if (mode != KeyboardMode.SYMBOL && mode != KeyboardMode.HANDWRITING && mode != KeyboardMode.CLIPBOARD && mode != KeyboardMode.PHRASES && !_isEnglishMode.value) {
            _previousMode.value = _currentMode.value
        }
        _currentMode.value = mode
    }

    fun setPreviousMode(mode: KeyboardMode) {
        _previousMode.value = mode
    }

    fun setEnglishMode(isEnglish: Boolean) {
        _isEnglishMode.value = isEnglish
        clearSession()
    }

    fun setBound(bound: Boolean) {
        engineAdapter.isBound = bound
    }

    fun destroy() {
        scope.cancel()
    }

    private fun updateRawInput(newInput: String) {
        _rawInput.value = newInput

        // Cancel previous job if it's still running
        searchJob?.cancel()

        if (newInput.isEmpty()) {
            _displayPinyinText.value = ""
            _candidateList.value = emptyList()
            _t9PinyinCombinations.value = emptyList()
            scope.launch { engineAdapter.resetSearch() }
            return
        }

        searchJob = scope.launch {
            // Debounce delay to prevent overwhelming the JNI C++ engine on fast typing
            // This ensures we only run nativeImSearch when the user pauses briefly
            delay(50)

            val result = engineAdapter.searchPinyin(newInput, isT9Mode)
            _displayPinyinText.value = result.first
            _candidateList.value = result.second
            _t9PinyinCombinations.value = result.third
        }
    }


    fun handleCursorMove(offset: Int) {
        commitPolicy.moveCursor(offset)
    }

    fun clearSession() {
        searchJob?.cancel()
        _rawInput.value = ""
        _displayPinyinText.value = ""
        _candidateList.value = emptyList()
        _t9PinyinCombinations.value = emptyList()
        scope.launch { engineAdapter.resetSearch() }
    }
}
