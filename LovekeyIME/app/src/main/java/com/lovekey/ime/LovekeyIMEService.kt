package com.lovekey.ime

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.inputmethodservice.InputMethodService
import android.os.IBinder
import android.view.View
import android.view.inputmethod.EditorInfo
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

class LovekeyIMEService : InputMethodService(), LifecycleOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

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
        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@LovekeyIMEService)
            setViewTreeSavedStateRegistryOwner(this@LovekeyIMEService)
            setContent {
                var currentPinyinText by remember { mutableStateOf("") }
                var candidateList by remember { mutableStateOf<List<String>>(emptyList()) }

                LovekeyKeyboard(
                    currentPinyinText = currentPinyinText,
                    candidateList = candidateList,
                    onKeyPress = { key ->
                        when (key) {
                            "DEL" -> {
                                if (currentPinyinText.isNotEmpty()) {
                                    currentPinyinText = currentPinyinText.dropLast(1)
                                    candidateList = updateCandidates(currentPinyinText)
                                } else {
                                    currentInputConnection?.deleteSurroundingText(1, 0)
                                }
                            }
                            "SPACE" -> {
                                if (candidateList.isNotEmpty()) {
                                    currentInputConnection?.commitText(candidateList.first(), 1)
                                    currentPinyinText = ""
                                    candidateList = emptyList()
                                    if (isBound) PinyinDecoderService.nativeImResetSearch()
                                } else {
                                    currentInputConnection?.commitText(" ", 1)
                                }
                            }
                            "ENT" -> {
                                if (currentPinyinText.isNotEmpty()) {
                                    currentInputConnection?.commitText(currentPinyinText, 1)
                                    currentPinyinText = ""
                                    candidateList = emptyList()
                                    if (isBound) PinyinDecoderService.nativeImResetSearch()
                                } else {
                                    currentInputConnection?.performEditorAction(EditorInfo.IME_ACTION_DONE)
                                }
                            }
                            else -> {
                                currentPinyinText += key
                                candidateList = updateCandidates(currentPinyinText)
                            }
                        }
                    },
                    onCandidateSelected = { candidate ->
                        currentInputConnection?.commitText(candidate, 1)
                        currentPinyinText = ""
                        candidateList = emptyList()
                        if (isBound) PinyinDecoderService.nativeImResetSearch()
                    }
                )
            }
        }
        return composeView
    }

    private fun updateCandidates(pinyin: String): List<String> {
        if (pinyin.isEmpty()) {
            if (isBound) PinyinDecoderService.nativeImResetSearch()
            return emptyList()
        }

        if (isBound && pinyinService != null) {
            val pyBytes = pinyin.toByteArray()
            val numPredicts = PinyinDecoderService.nativeImSearch(pyBytes, pyBytes.size)

            val newCandidates = mutableListOf<String>()
            val maxCandidates = minOf(numPredicts, 10)
            for (i in 0 until maxCandidates) {
                val choice = PinyinDecoderService.nativeImGetChoice(i)
                if (choice != null) {
                    newCandidates.add(choice)
                }
            }
            return newCandidates
        }
        return emptyList()
    }
}
