import re

with open('LovekeyIME/app/src/main/java/com/lovekey/ime/LovekeyIMEService.kt', 'r') as f:
    content = f.read()

# Add the T9 permutation logic inside LovekeyIMEService class
t9_logic = """
    private val t9Mapping = mapOf(
        '2' to listOf('a', 'b', 'c'),
        '3' to listOf('d', 'e', 'f'),
        '4' to listOf('g', 'h', 'i'),
        '5' to listOf('j', 'k', 'l'),
        '6' to listOf('m', 'n', 'o'),
        '7' to listOf('p', 'q', 'r', 's'),
        '8' to listOf('t', 'u', 'v'),
        '9' to listOf('w', 'x', 'y', 'z')
    )

    private fun getT9Permutations(digits: String): List<String> {
        if (digits.isEmpty()) return emptyList()
        var results = listOf("")
        for (digit in digits) {
            val letters = t9Mapping[digit] ?: continue
            val newResults = mutableListOf<String>()
            for (prefix in results) {
                for (letter in letters) {
                    newResults.add(prefix + letter)
                }
            }
            results = newResults
        }
        return results
    }

    private fun updateCandidates(input: String, isT9: Boolean = false): Pair<String, List<String>> {
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
                        allCandidates.add(Pair(pinyin, candidates))
                    }
                }

                if (allCandidates.isNotEmpty()) {
                    // For simplicity, pick the permutation that yielded the most/best results or just the first valid one
                    // Realistically, AOSP Pinyin has word frequencies. We take the one that produced results.
                    allCandidates.sortByDescending { it.second.size }
                    val bestMatch = allCandidates.first()
                    return Pair(bestMatch.first, bestMatch.second)
                }
                return Pair(input, emptyList())
            }
        }
        return Pair(input, emptyList())
    }
"""

content = re.sub(r'    private fun updateCandidates\(pinyin: String\): List<String> \{.*?\n    \}\n', t9_logic, content, flags=re.DOTALL)

# Now update the setContent block
# Find the setContent block
setContent_replacement = """            setContent {
                var rawInput by remember { mutableStateOf("") }
                var displayPinyinText by remember { mutableStateOf("") }
                var candidateList by remember { mutableStateOf<List<String>>(emptyList()) }
                var isT9Mode by remember { mutableStateOf(false) }

                LovekeyKeyboard(
                    currentPinyinText = displayPinyinText,
                    candidateList = candidateList,
                    onKeyPress = { key ->
                        when (key) {
                            "DEL" -> {
                                if (rawInput.isNotEmpty()) {
                                    rawInput = rawInput.dropLast(1)
                                    val result = updateCandidates(rawInput, isT9Mode)
                                    displayPinyinText = result.first
                                    candidateList = result.second
                                } else {
                                    currentInputConnection?.deleteSurroundingText(1, 0)
                                }
                            }
                            "SPACE" -> {
                                if (candidateList.isNotEmpty()) {
                                    currentInputConnection?.commitText(candidateList.first(), 1)
                                    rawInput = ""
                                    displayPinyinText = ""
                                    candidateList = emptyList()
                                    if (isBound) PinyinDecoderService.nativeImResetSearch()
                                } else {
                                    currentInputConnection?.commitText(" ", 1)
                                }
                            }
                            "ENT" -> {
                                if (displayPinyinText.isNotEmpty()) {
                                    currentInputConnection?.commitText(displayPinyinText, 1)
                                    rawInput = ""
                                    displayPinyinText = ""
                                    candidateList = emptyList()
                                    if (isBound) PinyinDecoderService.nativeImResetSearch()
                                } else {
                                    currentInputConnection?.performEditorAction(EditorInfo.IME_ACTION_DONE)
                                }
                            }
                            else -> {
                                // Detect if it's a digit from T9
                                val isDigit = key.length == 1 && key[0].isDigit() && key != "0" && key != "1"
                                // If we transition from QWERTY to T9 or vice versa, we might want to clear, but for now just append.
                                // We update isT9Mode based on whether the input so far contains numbers.
                                if (rawInput.isEmpty()) {
                                    isT9Mode = isDigit
                                }

                                rawInput += key.lowercase()
                                val result = updateCandidates(rawInput, isT9Mode)
                                displayPinyinText = result.first
                                candidateList = result.second
                            }
                        }
                    },
                    onCandidateSelected = { candidate ->
                        currentInputConnection?.commitText(candidate, 1)
                        rawInput = ""
                        displayPinyinText = ""
                        candidateList = emptyList()
                        if (isBound) PinyinDecoderService.nativeImResetSearch()
                    }
                )
            }"""

content = re.sub(r'            setContent \{.*?\n            \}', setContent_replacement, content, flags=re.DOTALL)

with open('LovekeyIME/app/src/main/java/com/lovekey/ime/LovekeyIMEService.kt', 'w') as f:
    f.write(content)
