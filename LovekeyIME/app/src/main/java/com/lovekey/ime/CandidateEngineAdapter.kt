package com.lovekey.ime

import com.android.inputmethod.pinyin.PinyinDecoderService
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

class CandidateEngineAdapter {
    var isBound = false

    var enableTypoCorrection = true
    var fuzzyZhZ = false
    var fuzzyChC = false
    var fuzzyShS = false
    var fuzzyNL = false
    var fuzzyEnEng = false
    var fuzzyInIng = false
    var fuzzyAnAng = false


    // Use a single-threaded dispatcher for all JNI calls to prevent concurrent
    // modification of the underlying C++ Pinyin engine state, which can cause segfaults.
    private val jniDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    // Cache for dead-end T9 paths. If a pinyin prefix yielded 0 results,
    // any longer string starting with this prefix is also a dead-end.
    private val deadEndPrefixes = mutableSetOf<String>()

    // Maps a selected candidate string to its source Pinyin variant and its actual JNI index
    private val candidateMemoryLookup = mutableMapOf<String, Pair<String, Int>>()


    // Basic fuzzy pinyin mappings
    private val fuzzyPairs = listOf(
        Pair("zh", "z"), Pair("ch", "c"), Pair("sh", "s"),
        Pair("l", "n"), Pair("h", "f"), Pair("eng", "en"), Pair("ing", "in"), Pair("ang", "an")
    )

    private fun generateFuzzyVariants(pinyin: String): List<String> {
        var variants = mutableSetOf(pinyin)
        for (pair in fuzzyPairs) {
            val newVariants = mutableSetOf<String>()
            for (variant in variants) {
                if (variant.contains(pair.first)) {
                    newVariants.add(variant.replace(pair.first, pair.second))
                }
                if (variant.contains(pair.second)) {
                    // Try replacing second with first. To be safe, let's only do targeted replacement
                    // Actually, simple .replace will replace ALL occurrences. We might want to just append common typo variants.
                    // For a robust but simple approach:
                }
            }
            variants.addAll(newVariants)
        }
        return variants.toList()
    }

    // Better approach:
    private fun expandFuzzyVariants(input: String): List<String> {
        val results = mutableSetOf(input)

        if (enableTypoCorrection) {
            val typoRules = mapOf(
                "gn" to "ng",
                "mg" to "ng",
                "iou" to "iu",
                "uei" to "ui",
                "uen" to "un"
            )
            for ((from, to) in typoRules) {
                if (input.endsWith(from)) {
                    results.add(input.substring(0, input.length - from.length) + to)
                } else if (input.contains(from)) {
                    results.add(input.replaceFirst(from, to))
                }
            }
        }

        val initialRules = mutableMapOf<String, String>()
        if (fuzzyZhZ) { initialRules["zh"] = "z"; initialRules["z"] = "zh" }
        if (fuzzyChC) { initialRules["ch"] = "c"; initialRules["c"] = "ch" }
        if (fuzzyShS) { initialRules["sh"] = "s"; initialRules["s"] = "sh" }
        if (fuzzyNL) { initialRules["n"] = "l"; initialRules["l"] = "n" }

        val finalRules = mutableMapOf<String, String>()
        if (fuzzyEnEng) { finalRules["eng"] = "en"; finalRules["en"] = "eng" }
        if (fuzzyInIng) { finalRules["ing"] = "in"; finalRules["in"] = "ing" }
        if (fuzzyAnAng) { finalRules["ang"] = "an"; finalRules["an"] = "ang" }

        val expandedFromTypos = results.toList()
        for (variant in expandedFromTypos) {
            for ((from, to) in initialRules) {
                if (variant.startsWith(from)) {
                    results.add(to + variant.substring(from.length))
                }
            }
            for ((from, to) in finalRules) {
                if (variant.endsWith(from)) {
                    results.add(variant.substring(0, variant.length - from.length) + to)
                }
            }
        }

        return results.take(6).toList()
    }

    suspend fun searchPinyin(input: String, isT9: Boolean = false): Triple<String, List<String>, List<String>> = withContext(jniDispatcher) {
        if (input.isEmpty()) {
            deadEndPrefixes.clear()
        candidateMemoryLookup.clear()
            resetSearchInternal()
            return@withContext Triple("", emptyList(), emptyList())
        }

        var t9Combinations = emptyList<String>()

        if (isBound) {
            if (!isT9) {
                // Normal QWERTY Pinyin Search
                val fuzzyVariants = expandFuzzyVariants(input)
                val allCandidates = mutableListOf<Pair<String, List<String>>>()

                for (variant in fuzzyVariants) {
                    ensureActive()
                    resetSearchInternal()
                    val pyBytes = variant.toByteArray()
                    val numPredicts = PinyinDecoderService.nativeImSearch(pyBytes, pyBytes.size)

                    if (numPredicts > 0) {
                        val candidates = mutableListOf<String>()
                        val maxCandidates = minOf(numPredicts, 10)
                        for (i in 0 until maxCandidates) {
                            ensureActive()
                            val choice = PinyinDecoderService.nativeImGetChoice(i)
                            if (choice != null) {
                                candidates.add(choice)
                                candidateMemoryLookup[choice] = Pair(variant, i)
                            }
                        }
                        if (candidates.isNotEmpty()) {
                            allCandidates.add(Pair(variant, candidates))
                        }
                    }
                }

                if (allCandidates.isNotEmpty()) {
                    // Flatten and remove duplicates, maintaining order of the best variant first
                    val flatCandidates = mutableListOf<String>()
                    var primaryMatchVariant = input
                    if (allCandidates.first().first != input && allCandidates.none { it.first == input }) {
                        primaryMatchVariant = allCandidates.first().first
                    }

                    for (match in allCandidates) {
                        ensureActive()
                        flatCandidates.addAll(match.second)
                    }
                    return@withContext Triple(primaryMatchVariant, flatCandidates.distinct(), t9Combinations)
                }

                return@withContext Triple(input, emptyList(), t9Combinations)
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
                                candidateMemoryLookup[choice] = Pair(pinyin, i)
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


    suspend fun chooseCandidate(candidate: String) = withContext(jniDispatcher) {
        if (!isBound) return@withContext
        val lookup = candidateMemoryLookup[candidate] ?: return@withContext
        val (variant, jniIndex) = lookup

        // Re-establish search state for the exact variant that generated this candidate
        PinyinDecoderService.nativeImResetSearch()
        val pyBytes = variant.toByteArray()
        val numPredicts = PinyinDecoderService.nativeImSearch(pyBytes, pyBytes.size)

        // Ensure the engine actually returned enough predictions for this index to be safe
        if (jniIndex < numPredicts) {
            PinyinDecoderService.nativeImChoose(jniIndex)
            PinyinDecoderService.nativeImFlushCache()
        }
    }

    suspend fun flushCache() = withContext(jniDispatcher) {
        if (!isBound) return@withContext
        PinyinDecoderService.nativeImFlushCache()
    }

    suspend fun resetSearch() = withContext(jniDispatcher) {
        deadEndPrefixes.clear()
        candidateMemoryLookup.clear()
        resetSearchInternal()
    }

    private fun resetSearchInternal() {
        if (isBound) {
            PinyinDecoderService.nativeImResetSearch()
        }
    }
}
