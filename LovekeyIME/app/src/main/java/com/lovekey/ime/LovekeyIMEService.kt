package com.lovekey.ime

import android.content.ComponentName
import android.content.Context
import android.content.ClipboardManager
import android.content.ClipData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.lovekey.ime.theme.ThemePresets
import com.lovekey.ime.theme.PersonaTheme
import kotlinx.coroutines.launch
import androidx.datastore.preferences.core.edit

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.map
import com.lovekey.ime.ui.host.dataStore
import com.lovekey.ime.ui.host.SettingsKeys


class LovekeyIMEService : InputMethodService(), LifecycleOwner, SavedStateRegistryOwner, ViewModelStoreOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override val viewModelStore: ViewModelStore
        get() = store

    private var pinyinService: PinyinDecoderService? = null
    private var isBound = false
    private val editorInfoState = mutableStateOf<EditorInfo?>(null)
    private val personaThemeState = mutableStateOf(ThemePresets.ThemeGirl)

        private val _clipboardHistory = MutableStateFlow<List<String>>(emptyList())
    val clipboardHistory = _clipboardHistory.asStateFlow()

    private var clipboardManager: ClipboardManager? = null

    private val clipboardListener = ClipboardManager.OnPrimaryClipChangedListener {
        val clipData = clipboardManager?.primaryClip
        if (clipData != null && clipData.itemCount > 0) {
            val text = clipData.getItemAt(0).text?.toString()
            if (!text.isNullOrBlank()) {
                val truncatedText = if (text.length > 5000) text.substring(0, 5000) + "..." else text
                serviceScope.launch {
                    dataStore.edit { prefs ->
                        val currentJsonStr = prefs[SettingsKeys.CLIPBOARD_HISTORY] ?: "[]"
                        val currentList = try {
                            val jsonArray = JSONArray(currentJsonStr)
                            val list = mutableListOf<String>()
                            for (i in 0 until jsonArray.length()) {
                                list.add(jsonArray.getString(i))
                            }
                            list
                        } catch (e: Exception) { mutableListOf<String>() }

                        // Remove if exists to push to front
                        currentList.remove(truncatedText)
                        currentList.add(0, truncatedText)

                        // Keep max 20 items
                        val newList = currentList.take(20)
                        prefs[SettingsKeys.CLIPBOARD_HISTORY] = JSONArray(newList).toString()
                    }
                }
            }
        }
    }



    private fun removeClip(text: String) {
        serviceScope.launch {
            dataStore.edit { prefs ->
                val currentJsonStr = prefs[SettingsKeys.CLIPBOARD_HISTORY] ?: "[]"
                val currentList = try {
                    val jsonArray = JSONArray(currentJsonStr)
                    val list = mutableListOf<String>()
                    for (i in 0 until jsonArray.length()) {
                        list.add(jsonArray.getString(i))
                    }
                    list
                } catch (e: Exception) { mutableListOf<String>() }

                currentList.remove(text)
                prefs[SettingsKeys.CLIPBOARD_HISTORY] = JSONArray(currentList).toString()
            }
        }
    }

    private fun clearClipboard() {
        serviceScope.launch {
            dataStore.edit { prefs ->
                prefs[SettingsKeys.CLIPBOARD_HISTORY] = "[]"
            }
        }
    }

    private val engineAdapter = CandidateEngineAdapter()



    private val commitPolicy = CommitPolicy()

    private val serviceJob = kotlinx.coroutines.Job()
    private val serviceScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main + serviceJob)

        private val sessionController by lazy {
        InputSessionController(engineAdapter, commitPolicy) { delta ->
            serviceScope.launch {
                dataStore.edit { prefs ->
                                        val current: Int = prefs[SettingsKeys.AFFECTION_SCORE] ?: 150
                    // Score clamped between 0 and 1000
                    prefs[SettingsKeys.AFFECTION_SCORE] = (current + delta).coerceIn(0, 1000)
                }
            }
        }
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as PinyinDecoderService.LocalBinder
            pinyinService = binder.getService()
            isBound = true
            sessionController.setBound(true)
            pinyinService?.initPinyinEngine()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
            sessionController.setBound(false)
        }
    }
    override fun onCreate() {
        super.onCreate()
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager?.addPrimaryClipChangedListener(clipboardListener)
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        serviceScope.launch {
            dataStore.data.collect { prefs ->
                engineAdapter.enableTypoCorrection = prefs[SettingsKeys.ENABLE_TYPO_CORRECTION] ?: true
                engineAdapter.fuzzyZhZ = prefs[SettingsKeys.FUZZY_ZH_Z] ?: false
                engineAdapter.fuzzyChC = prefs[SettingsKeys.FUZZY_CH_C] ?: false
                engineAdapter.fuzzyShS = prefs[SettingsKeys.FUZZY_SH_S] ?: false
                engineAdapter.fuzzyNL = prefs[SettingsKeys.FUZZY_N_L] ?: false
                engineAdapter.fuzzyEnEng = prefs[SettingsKeys.FUZZY_EN_ENG] ?: false
                engineAdapter.fuzzyInIng = prefs[SettingsKeys.FUZZY_IN_ING] ?: false
                                engineAdapter.fuzzyAnAng = prefs[SettingsKeys.FUZZY_AN_ANG] ?: false

                val historyJsonStr = prefs[SettingsKeys.CLIPBOARD_HISTORY] ?: "[]"
                try {
                    val jsonArray = JSONArray(historyJsonStr)
                    val list = mutableListOf<String>()
                    for (i in 0 until jsonArray.length()) {
                        list.add(jsonArray.getString(i))
                    }
                    _clipboardHistory.value = list
                } catch (e: Exception) {
                    _clipboardHistory.value = emptyList()
                }

                val personaId = prefs[SettingsKeys.PERSONA_ID] ?: "theme_girl"
                personaThemeState.value = ThemePresets.getThemeById(personaId)
            }
        }


        Intent(this, PinyinDecoderService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
        sessionController.destroy()

        window?.window?.decorView?.let { view ->
            view.setViewTreeLifecycleOwner(null)
            view.setViewTreeSavedStateRegistryOwner(null)
            view.setViewTreeViewModelStoreOwner(null)
        }

        if (isBound) {
            unbindService(connection)
            isBound = false
            sessionController.setBound(false)
        }
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        editorInfoState.value = info
        commitPolicy.editorInfo = info
        commitPolicy.inputConnection = currentInputConnection
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        commitPolicy.inputConnection = null
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
                val editorInfo = editorInfoState.value
                val enterKeyText = remember(editorInfo) {
                    val imeOptions = editorInfo?.imeOptions ?: 0
                    val actionMasked = imeOptions and EditorInfo.IME_MASK_ACTION
                    when (actionMasked) {
                        EditorInfo.IME_ACTION_GO -> "前往"
                        EditorInfo.IME_ACTION_NEXT -> "下一项"
                        EditorInfo.IME_ACTION_SEARCH -> "搜索"
                        EditorInfo.IME_ACTION_SEND -> "发送"
                        EditorInfo.IME_ACTION_DONE -> "完成"
                        EditorInfo.IME_ACTION_PREVIOUS -> "上一项"
                        else -> {
                            if ((editorInfo?.inputType ?: 0) and android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE != 0) {
                                "换行"
                            } else {
                                "发送"
                            }
                        }
                    }
                }

                val displayPinyinText by sessionController.displayPinyinText.collectAsState()
                val candidateList by sessionController.candidateList.collectAsState()
                val t9PinyinCombinations by sessionController.t9PinyinCombinations.collectAsState()

                val isEnglishMode by sessionController.isEnglishMode.collectAsState()
                val currentMode by sessionController.currentMode.collectAsState()
                val previousMode by sessionController.previousMode.collectAsState()
                val currentClipboardHistory by clipboardHistory.collectAsState()


                val theme = personaThemeState.value
                LovekeyKeyboard(
                    currentPinyinText = displayPinyinText,
                    candidateList = candidateList,
                    t9PinyinCombinations = t9PinyinCombinations,
                    currentModeExternal = currentMode,
                    previousModeExternal = previousMode,
                    isEnglishModeExternal = isEnglishMode,
                    enterKeyText = enterKeyText,
                    boardColor = theme.boardColor,
                    keyColor = theme.keyColor,
                    functionKeyColor = theme.functionKeyColor,
                    accentColor = theme.accentColor,
                    textColor = theme.textColor,
                    secondaryTextColor = theme.secondaryTextColor,
                    unselectedTabColor = theme.unselectedTabColor,
                    onEnglishModeChanged = { sessionController.setEnglishMode(it) },
                    onKeyboardModeChanged = { sessionController.setKeyboardMode(it) },
                    onKeyPress = { key -> sessionController.handleKeyPress(key) },
                    onCandidateSelected = { candidate -> sessionController.handleCandidateSelected(candidate) },
                    onSyllableSelected = { syllable -> sessionController.handleSyllableSelected(syllable) },
                    onCursorMove = { offset -> sessionController.handleCursorMove(offset) },
                    clipboardHistory = currentClipboardHistory,
                    onPasteClip = { text -> commitPolicy.commitText(text) },
                    onDeleteClip = { text -> removeClip(text) },
                    onClearClipboard = { clearClipboard() }
                )
            }
        }

        rootLayout.addView(composeView)
        rootLayout.setViewTreeLifecycleOwner(this@LovekeyIMEService)
        rootLayout.setViewTreeSavedStateRegistryOwner(this@LovekeyIMEService)
        rootLayout.setViewTreeViewModelStoreOwner(this@LovekeyIMEService)

        return rootLayout
    }
}
