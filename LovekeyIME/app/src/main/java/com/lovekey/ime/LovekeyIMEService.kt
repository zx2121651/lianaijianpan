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

    private val engineAdapter = CandidateEngineAdapter()
    private val commitPolicy = CommitPolicy()
    private val sessionController = InputSessionController(engineAdapter, commitPolicy)

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


                LovekeyKeyboard(
                    currentPinyinText = displayPinyinText,
                    candidateList = candidateList,
                    t9PinyinCombinations = t9PinyinCombinations,
                    isEnglishModeExternal = isEnglishMode,
                    enterKeyText = enterKeyText,
                    onEnglishModeChanged = { sessionController.setEnglishMode(it) },
                    onKeyboardModeChanged = { sessionController.setKeyboardMode(it) },
                    onKeyPress = { key -> sessionController.handleKeyPress(key) },
                    onCandidateSelected = { candidate -> sessionController.handleCandidateSelected(candidate) },
                    onSyllableSelected = { syllable -> sessionController.handleSyllableSelected(syllable) }
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
