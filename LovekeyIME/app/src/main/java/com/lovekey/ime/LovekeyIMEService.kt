package com.lovekey.ime

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.inputmethodservice.InputMethodService
import android.os.IBinder
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.compose.ui.platform.ComposeView
import androidx.compose.runtime.*
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import com.android.inputmethod.pinyin.PinyinDecoderService
import com.lovekey.ime.ui.LovekeyKeyboard

class LovekeyIMEService : InputMethodService(), LifecycleOwner, SavedStateRegistryOwner, ViewModelStoreOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
private val store = ViewModelStore()
    override val viewModelStore: ViewModelStore
        get() = store

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    private var pinyinService: PinyinDecoderService? = null
    private var isBound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as PinyinDecoderService.LocalBinder
            pinyinService = binder.getService()
            isBound = true
            pinyinService?.initPinyinEngine()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
        }
    }

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        Intent(this, PinyinDecoderService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()

        // Remove window extensions to avoid leaks
        window?.window?.decorView?.let { view ->
            view.setViewTreeLifecycleOwner(null)
            view.setViewTreeSavedStateRegistryOwner(null)
            view.setViewTreeViewModelStoreOwner(null)
        }

        if (isBound) {
            unbindService(connection)
            isBound = false
        }
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    override fun onCreateInputView(): View {
        val rootLayout = FrameLayout(this)

        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@LovekeyIMEService)
            setViewTreeSavedStateRegistryOwner(this@LovekeyIMEService)
            setViewTreeViewModelStoreOwner(this@LovekeyIMEService)

            setContent {
                var rawInput by remember { mutableStateOf("") }
                var displayPinyinText by remember { mutableStateOf("") }
                var candidateList by remember { mutableStateOf<List<String>>(emptyList()) }
                var isT9Mode by remember { mutableStateOf(false) }
                var t9PinyinCombinations by remember { mutableStateOf<List<String>>(emptyList()) }

                LovekeyKeyboard(
                    currentPinyinText = displayPinyinText,
                    candidateList = candidateList,
                    t9PinyinCombinations = t9PinyinCombinations,
                    onKeyPress = { key ->
                        when (key) {
                            ",", ".", "?", "!" -> {
                                val chinesePunct = when (key) {
                                    "," -> "，"
                                    "." -> "。"
                                    "?" -> "？"
                                    "!" -> "！"
                                    else -> key
                                }

                                // If there are active candidates, commit the first one before punctuation
                                if (candidateList.isNotEmpty()) {
                                    currentInputConnection?.commitText(candidateList.first(), 1)
                                } else if (rawInput.isNotEmpty()) {
                                    currentInputConnection?.commitText(rawInput, 1)
                                }

                                currentInputConnection?.commitText(chinesePunct, 1)
                                rawInput = ""
                                displayPinyinText = ""
                                candidateList = emptyList()
                                t9PinyinCombinations = emptyList<String>()
                                if (isBound) PinyinDecoderService.nativeImResetSearch()
                            }
                            "1" -> {
                                if (rawInput.isEmpty()) {
                                    // If typing nothing, maybe output default punctuation or do nothing.
                                    // Let's mimic Sogou: if no input, usually it's a punctuation short-cut.
                                    // For simplicity, we can output a default full-width comma or do nothing.
                                    currentInputConnection?.commitText("，", 1)
                                } else {
                                    // Acting as a separator
                                    rawInput += "1"
                                    val result = updateCandidates(rawInput, isT9Mode)
                                    displayPinyinText = result.first
                                    candidateList = result.second
                                    t9PinyinCombinations = result.third
                                }
                            }
                            "CLEAR" -> {
                                rawInput = ""
                                displayPinyinText = ""
                                candidateList = emptyList()
                                t9PinyinCombinations = emptyList<String>()
                                if (isBound) PinyinDecoderService.nativeImResetSearch()
                            }
                            "DEL" -> {
                                if (rawInput.isNotEmpty()) {
                                    rawInput = rawInput.dropLast(1)
                                    val result = updateCandidates(rawInput, isT9Mode)
                                    displayPinyinText = result.first
                                    candidateList = result.second
                                    t9PinyinCombinations = result.third
                                } else {
                                    currentInputConnection?.deleteSurroundingText(1, 0)
                                }
                            }
                            "SPACE" -> {
                                if (candidateList.isNotEmpty()) {
                                    currentInputConnection?.commitText(candidateList.first(), 1)
                                    rawInput = ""
                                    displayPinyinText = ""
                                    candidateList = emptyList()
                                    if (isBound) PinyinDecoderService.nativeImResetSearch()
                                } else {
                                    currentInputConnection?.commitText(" ", 1)
                                }
                            }
                            "ENT" -> {
                                if (displayPinyinText.isNotEmpty()) {
                                    currentInputConnection?.commitText(displayPinyinText, 1)
                                    rawInput = ""
                                    displayPinyinText = ""
                                    candidateList = emptyList()
                                    if (isBound) PinyinDecoderService.nativeImResetSearch()
                                } else {
                                    currentInputConnection?.performEditorAction(EditorInfo.IME_ACTION_DONE)
                                }
                            }
                            "中/英" -> {
                                // Toggle Chinese/English mode (Placeholder for future implementation)
                                // Currently we just clear or ignore.
                            }
                            else -> {
                                // Detect if it's a digit from T9
                                val isDigit = key.length == 1 && key[0].isDigit() && key != "0" && key != "1"
                                // If we transition from QWERTY to T9 or vice versa, we might want to clear, but for now just append.
                                // We update isT9Mode based on whether the input so far contains numbers.
                                if (rawInput.isEmpty()) {
                                    isT9Mode = isDigit
                                }

                                rawInput += key.lowercase()
                                val result = updateCandidates(rawInput, isT9Mode)
                                displayPinyinText = result.first
                                candidateList = result.second
                                t9PinyinCombinations = result.third
                            }
                        }
                    },
                    onCandidateSelected = { candidate ->
                        currentInputConnection?.commitText(candidate, 1)
                        rawInput = ""
                        displayPinyinText = ""
                        candidateList = emptyList()
                        t9PinyinCombinations = emptyList()
                        if (isBound) PinyinDecoderService.nativeImResetSearch()
                    },
                    onSyllableSelected = { selectedSyllable ->
                        displayPinyinText = selectedSyllable
                        if (isBound) {
                            PinyinDecoderService.nativeImResetSearch()
                            val pyBytes = selectedSyllable.toByteArray()
                            val numPredicts = PinyinDecoderService.nativeImSearch(pyBytes, pyBytes.size)
                            val newCandidates = mutableListOf<String>()
                            val maxCandidates = minOf(numPredicts, 10)
                            for (i in 0 until maxCandidates) {
                                val choice = PinyinDecoderService.nativeImGetChoice(i)
                                if (choice != null) {
                                    newCandidates.add(choice)
                                }
                            }
                            candidateList = newCandidates
                        }
                    }
                )
            }
        }
        rootLayout.addView(composeView)

        rootLayout.setViewTreeLifecycleOwner(this@LovekeyIMEService)
        rootLayout.setViewTreeSavedStateRegistryOwner(this@LovekeyIMEService)
        rootLayout.setViewTreeViewModelStoreOwner(this@LovekeyIMEService)

        return rootLayout
    }


    private val t9Mapping = mapOf(
        '2' to listOf("a", "b", "c"),
        '3' to listOf("d", "e", "f"),
        '4' to listOf("g", "h", "i"),
        '5' to listOf("j", "k", "l"),
        '6' to listOf("m", "n", "o"),
        '7' to listOf("p", "q", "r", "s"),
        '8' to listOf("t", "u", "v"),
        '9' to listOf("w", "x", "y", "z")
    )

    private fun getT9Permutations(digits: String): List<String> {
        if (digits.isEmpty()) return emptyList()
        var results = listOf("")
        for (digit in digits) {
            val letters = t9Mapping[digit] ?: continue
            val newResults = mutableListOf<String>()
            for (prefix in results) {
                for (letter in letters) {
                    newResults.add(prefix + letter)
                }
            }
            results = newResults
        }
        return results
    }

private fun updateCandidates(input: String, isT9: Boolean = false): Triple<String, List<String>, List<String>> {
        if (input.isEmpty()) {
            if (isBound) PinyinDecoderService.nativeImResetSearch()
            return Triple("", emptyList(), emptyList())
        }

        var t9Combinations = emptyList<String>()

        if (isBound && pinyinService != null) {
            if (!isT9) {
                // Normal QWERTY Pinyin Search
                val pyBytes = input.toByteArray()
                val numPredicts = PinyinDecoderService.nativeImSearch(pyBytes, pyBytes.size)

                val newCandidates = mutableListOf<String>()
                val maxCandidates = minOf(numPredicts, 10)
                for (i in 0 until maxCandidates) {
                    val choice = PinyinDecoderService.nativeImGetChoice(i)
                    if (choice != null) {
                        newCandidates.add(choice)
                    }
                }
                return Triple(input, newCandidates, t9Combinations)
            } else {
                // T9 Sequence Search using T9Parser
                t9Combinations = T9Parser.getValidPinyins(input)
                val allCandidates = mutableListOf<Pair<String, List<String>>>()

                for (pinyin in t9Combinations) {
                    PinyinDecoderService.nativeImResetSearch()
                    val pyBytes = pinyin.toByteArray()
                    val numPredicts = PinyinDecoderService.nativeImSearch(pyBytes, pyBytes.size)

                    if (numPredicts > 0) {
                        val candidates = mutableListOf<String>()
                        val maxCandidates = minOf(numPredicts, 5) // Take top 5 from each valid permutation
                        for (i in 0 until maxCandidates) {
                            val choice = PinyinDecoderService.nativeImGetChoice(i)
                            if (choice != null) {
                                candidates.add(choice)
                            }
                        }
                        if (candidates.isNotEmpty()) {
                            allCandidates.add(Pair(pinyin, candidates))
                        }
                    }
                }

                if (allCandidates.isNotEmpty()) {
                    // For simplicity, pick the permutation that yielded the most/best results
                    allCandidates.sortByDescending { it.second.size }
                    val bestMatch = allCandidates.first()

                    // Combine all valid candidate lists into one single list
                    val flatCandidates = mutableListOf<String>()
                    for (match in allCandidates) {
                        flatCandidates.addAll(match.second)
                    }
                    return Triple(bestMatch.first, flatCandidates.distinct(), t9Combinations)
                }
            }
        }
        return Triple(input, emptyList(), t9Combinations)
    }
}
