package com.lovekey.ime

import com.android.inputmethod.pinyin.PinyinDecoderService
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

class CandidateEngineAdapter {
    var isBound = false

    // Use a single-threaded dispatcher for all JNI calls to prevent concurrent
    // modification of the underlying C++ Pinyin engine state, which can cause segfaults.
    private val jniDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    // Cache for dead-end T9 paths. If a pinyin prefix yielded 0 results,
    // any longer string starting with this prefix is also a dead-end.
    private val deadEndPrefixes = mutableSetOf<String>()

    suspend fun searchPinyin(input: String, isT9: Boolean = false): Triple<String, List<String>, List<String>> = withContext(jniDispatcher) {
        if (input.isEmpty()) {
            deadEndPrefixes.clear()
            resetSearchInternal()
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

                    // Pruning optimization: Skip branches known to be dead-ends
                    if (deadEndPrefixes.any { pinyin.startsWith(it) }) {
                        continue
                    }

                    resetSearchInternal()
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
                    } else {
                        // Mark as dead-end so future longer queries bypass this JNI bottleneck
                        deadEndPrefixes.add(pinyin)
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

    suspend fun searchBySyllable(syllable: String): List<String> = withContext(jniDispatcher) {
        if (!isBound) return@withContext emptyList()
        resetSearchInternal()
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

    suspend fun resetSearch() = withContext(jniDispatcher) {
        deadEndPrefixes.clear()
        resetSearchInternal()
    }

    private fun resetSearchInternal() {
        if (isBound) {
            PinyinDecoderService.nativeImResetSearch()
        }
    }
}
