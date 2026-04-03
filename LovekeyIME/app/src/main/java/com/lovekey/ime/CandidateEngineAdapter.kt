package com.lovekey.ime

import com.android.inputmethod.pinyin.PinyinDecoderService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.ensureActive

class CandidateEngineAdapter {
    var isBound = false

    suspend fun searchPinyin(input: String, isT9: Boolean = false): Triple<String, List<String>, List<String>> = withContext(Dispatchers.Default) {
        if (input.isEmpty()) {
            resetSearch()
            return@withContext Triple("", emptyList(), emptyList())
        }

        var t9Combinations = emptyList<String>()

        if (isBound) {
            if (!isT9) {
                // Normal QWERTY Pinyin Search
                val pyBytes = input.toByteArray()
                val numPredicts = PinyinDecoderService.nativeImSearch(pyBytes, pyBytes.size)

                val newCandidates = mutableListOf<String>()
                val maxCandidates = minOf(numPredicts, 10)
                for (i in 0 until maxCandidates) {
                    ensureActive() // Check for cancellation
                    val choice = PinyinDecoderService.nativeImGetChoice(i)
                    if (choice != null) {
                        newCandidates.add(choice)
                    }
                }
                return@withContext Triple(input, newCandidates, t9Combinations)
            } else {
                // T9 Sequence Search using T9Parser
                t9Combinations = T9Parser.getValidPinyins(input)
                val allCandidates = mutableListOf<Pair<String, List<String>>>()

                for (pinyin in t9Combinations) {
                    ensureActive() // Check for cancellation
                    resetSearch()
                    val pyBytes = pinyin.toByteArray()
                    val numPredicts = PinyinDecoderService.nativeImSearch(pyBytes, pyBytes.size)

                    if (numPredicts > 0) {
                        val candidates = mutableListOf<String>()
                        val maxCandidates = minOf(numPredicts, 5) // Take top 5 from each valid permutation
                        for (i in 0 until maxCandidates) {
                            ensureActive() // Check for cancellation inside inner loop too
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
                        ensureActive()
                        flatCandidates.addAll(match.second)
                    }
                    return@withContext Triple(bestMatch.first, flatCandidates.distinct(), t9Combinations)
                }
            }
        }
        return@withContext Triple(input, emptyList(), t9Combinations)
    }

    suspend fun searchBySyllable(syllable: String): List<String> = withContext(Dispatchers.Default) {
        if (!isBound) return@withContext emptyList()
        resetSearch()
        val pyBytes = syllable.toByteArray()
        val numPredicts = PinyinDecoderService.nativeImSearch(pyBytes, pyBytes.size)
        val newCandidates = mutableListOf<String>()
        val maxCandidates = minOf(numPredicts, 10)
        for (i in 0 until maxCandidates) {
            ensureActive()
            val choice = PinyinDecoderService.nativeImGetChoice(i)
            if (choice != null) {
                newCandidates.add(choice)
            }
        }
        return@withContext newCandidates
    }

    fun resetSearch() {
        if (isBound) {
            PinyinDecoderService.nativeImResetSearch()
        }
    }
}
