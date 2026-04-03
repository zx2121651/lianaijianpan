package com.lovekey.ime

import com.android.inputmethod.pinyin.PinyinDecoderService

class CandidateEngineAdapter {
    var isBound = false

    fun searchPinyin(input: String, isT9: Boolean = false): Triple<String, List<String>, List<String>> {
        if (input.isEmpty()) {
            resetSearch()
            return Triple("", emptyList(), emptyList())
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
                    resetSearch()
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

    fun searchBySyllable(syllable: String): List<String> {
        if (!isBound) return emptyList()
        resetSearch()
        val pyBytes = syllable.toByteArray()
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

    fun resetSearch() {
        if (isBound) {
            PinyinDecoderService.nativeImResetSearch()
        }
    }
}
