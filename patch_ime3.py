with open('LovekeyIME/app/src/main/java/com/lovekey/ime/LovekeyIMEService.kt', 'r') as f:
    content = f.read()

# Fix updateCandidates logic to correctly build candidates list without syntax errors
# and actually return from the branch correctly
logic = """    private fun updateCandidates(input: String, isT9: Boolean = false): Pair<String, List<String>> {
        if (input.isEmpty()) {
            if (isBound) PinyinDecoderService.nativeImResetSearch()
            return Pair("", emptyList())
        }

        if (isBound && pinyinService != null) {
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
                return Pair(input, newCandidates)
            } else {
                // T9 Sequence Search
                val permutations = getT9Permutations(input)
                val allCandidates = mutableListOf<Pair<String, List<String>>>()

                for (pinyin in permutations) {
                    PinyinDecoderService.nativeImResetSearch()
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
                    return Pair(bestMatch.first, flatCandidates.distinct())
                }
            }
        }
        return Pair(input, emptyList())
    }"""

import re
content = re.sub(r'    private fun updateCandidates\(input: String, isT9: Boolean = false\): Pair<String, List<String>> \{.*?\n    \}\n', logic + "\n", content, flags=re.DOTALL)

with open('LovekeyIME/app/src/main/java/com/lovekey/ime/LovekeyIMEService.kt', 'w') as f:
    f.write(content)
