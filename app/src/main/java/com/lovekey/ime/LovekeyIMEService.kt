package com.lovekey.ime

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.inputmethodservice.InputMethodService
import android.os.IBinder
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.android.inputmethod.pinyin.PinyinDecoderService

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

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as PinyinDecoderService.LocalBinder
            pinyinService = binder.getService()
            isBound = true
            Log.d("LovekeyIME", "PinyinDecoderService connected")
            pinyinService?.initPinyinEngine()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
            pinyinService = null
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("LovekeyIME", "onCreate called")
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
        store.clear()
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
        Log.d("LovekeyIME", "onCreateInputView called")

        // 关键修复：为输入法窗口的 DecorView 设置 Owner
        // getWindow() 返回 SoftInputWindow，它是一个 Dialog，我们需要拿到它的 Window 对象的 DecorView
        window?.window?.decorView?.let { decorView ->
            decorView.setViewTreeLifecycleOwner(this)
            decorView.setViewTreeSavedStateRegistryOwner(this)
            decorView.setViewTreeViewModelStoreOwner(this)
        }
        
        val composeView = ComposeView(this).apply {
            // 使用 DisposeOnDetachedFromWindow 策略在输入法关闭时释放资源
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            
            // 同时也为自身设置，确保万无一失
            setViewTreeLifecycleOwner(this@LovekeyIMEService)
            setViewTreeSavedStateRegistryOwner(this@LovekeyIMEService)
            setViewTreeViewModelStoreOwner(this@LovekeyIMEService)
            
            setContent {
                MaterialTheme {
                    Surface(color = Color(0xFFE0E0E0)) {
                        KeyboardContent()
                    }
                }
            }
        }
        return composeView
    }

    @Composable
    private fun KeyboardContent() {
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

@Composable
fun LovekeyKeyboard(
    currentPinyinText: String,
    candidateList: List<String>,
    onKeyPress: (String) -> Unit,
    onCandidateSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(bottom = 8.dp)
    ) {
        // Candidate View
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Color.White),
            contentAlignment = Alignment.CenterStart
        ) {
            if (currentPinyinText.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentPinyinText,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        fontSize = 14.sp
                    )
                    LazyRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(candidateList) { candidate ->
                            Text(
                                text = candidate,
                                fontSize = 18.sp,
                                modifier = Modifier
                                    .clickable { onCandidateSelected(candidate) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Keyboard View
        val rows = listOf(
            listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
            listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
            listOf("z", "x", "c", "v", "b", "n", "m", "DEL"),
            listOf(",", "SPACE", ".", "ENT")
        )

        rows.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { key ->
                    val weight = if (key == "SPACE") 4f else 1f
                    Box(
                        modifier = Modifier
                            .weight(weight)
                            .padding(horizontal = 2.dp)
                            .height(55.dp)
                            .background(if (key == "DEL" || key == "ENT") Color.LightGray else Color.White)
                            .clickable { onKeyPress(key) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = key,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
