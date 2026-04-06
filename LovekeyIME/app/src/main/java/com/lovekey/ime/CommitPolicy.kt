package com.lovekey.ime

import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection

class CommitPolicy {
    var inputConnection: InputConnection? = null
    var editorInfo: EditorInfo? = null

    fun commitText(text: String, newCursorPosition: Int = 1) {
        inputConnection?.commitText(text, newCursorPosition)
    }

    fun deleteSurroundingText(beforeLength: Int, afterLength: Int) {
        inputConnection?.deleteSurroundingText(beforeLength, afterLength)
    }

    fun performEditorAction(action: Int) {
        inputConnection?.performEditorAction(action)
    }

    /**
     * 处理中英文标点智能转换，并在上屏前发送候选词
     */
    fun commitPunctuation(key: String, isEnglishMode: Boolean, hasCandidates: Boolean, firstCandidate: String?, rawInput: String) {
        if (isEnglishMode) {
            commitText(key)
        } else {
            val chinesePunct = when (key) {
                "," -> "，"
                "." -> "。"
                "?" -> "？"
                "!" -> "！"
                else -> key
            }

            // If there are active candidates, commit the first one before punctuation
            if (hasCandidates && firstCandidate != null) {
                commitText(firstCandidate)
            } else if (rawInput.isNotEmpty()) {
                commitText(rawInput)
            }

            commitText(chinesePunct)
        }
    }

    /**
     * 处理动态回车键
     */

    fun moveCursor(offset: Int) {
        val request = android.view.inputmethod.ExtractedTextRequest()
        val text = inputConnection?.getExtractedText(request, 0)
        if (text != null) {
            val oldPos = text.selectionStart
            val newPos = (oldPos + offset).coerceIn(0, text.text.length)
            inputConnection?.setSelection(newPos, newPos)
        }
    }

    fun commitNewlineOrAction() {
        val action = editorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION) ?: EditorInfo.IME_ACTION_DONE
        if (action == EditorInfo.IME_ACTION_NONE || action == EditorInfo.IME_ACTION_UNSPECIFIED) {
            commitText("\n")
        } else {
            performEditorAction(action)
        }
    }
}
