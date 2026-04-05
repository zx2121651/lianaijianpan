package com.lovekey.ime

import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import io.mockk.*
import org.junit.Before
import org.junit.Test

class CommitPolicyTest {

    private lateinit var policy: CommitPolicy
    private lateinit var mockInputConnection: InputConnection
    private lateinit var mockEditorInfo: EditorInfo

    @Before
    fun setUp() {
        mockInputConnection = mockk(relaxed = true)
        mockEditorInfo = mockk(relaxed = true)
        policy = CommitPolicy()
        policy.inputConnection = mockInputConnection
        policy.editorInfo = mockEditorInfo
    }

    @Test
    fun `commitText correctly commits text to InputConnection`() {
        policy.commitText("hello")
        verify(exactly = 1) { mockInputConnection.commitText("hello", 1) }
    }

    @Test
    fun `commitPunctuation maps english punctuation to chinese when not in english mode`() {
        policy.commitPunctuation(",", false, false, null, "")
        verify(exactly = 1) { mockInputConnection.commitText("，", 1) }

        policy.commitPunctuation(".", false, false, null, "")
        verify(exactly = 1) { mockInputConnection.commitText("。", 1) }

        policy.commitPunctuation("?", false, false, null, "")
        verify(exactly = 1) { mockInputConnection.commitText("？", 1) }
    }

    @Test
    fun `commitPunctuation flushes candidate then commits punctuation`() {
        policy.commitPunctuation(",", false, true, "你好", "nihao")
        verifyOrder {
            mockInputConnection.commitText("你好", 1)
            mockInputConnection.commitText("，", 1)
        }
    }

    @Test
    fun `commitPunctuation uses raw punctuation when in english mode`() {
        policy.commitPunctuation(",", true, false, null, "")
        verify(exactly = 1) { mockInputConnection.commitText(",", 1) }
    }

    @Test
    fun `commitNewlineOrAction uses action when available`() {
        mockEditorInfo.imeOptions = EditorInfo.IME_ACTION_SEARCH
        policy.commitNewlineOrAction()
        verify(exactly = 1) { mockInputConnection.performEditorAction(EditorInfo.IME_ACTION_SEARCH) }
    }

    @Test
    fun `commitNewlineOrAction uses newline when action is UNSPECIFIED`() {
        mockEditorInfo.imeOptions = EditorInfo.IME_ACTION_UNSPECIFIED
        policy.commitNewlineOrAction()
        verify(exactly = 1) { mockInputConnection.commitText("\n", 1) }
    }
}
