import re

with open('LovekeyIME/app/src/main/java/com/lovekey/ime/LovekeyIMEService.kt', 'r') as f:
    content = f.read()

# Fix the method declaration
content = content.replace(
"""    private val t9Mapping = mapOf(
        '2' to listOf('a', 'b', 'c'),
        '3' to listOf('d', 'e', 'f'),
        '4' to listOf('g', 'h', 'i'),
        '5' to listOf('j', 'k', 'l'),
        '6' to listOf('m', 'n', 'o'),
        '7' to listOf('p', 'q', 'r', 's'),
        '8' to listOf('t', 'u', 'v'),
        '9' to listOf('w', 'x', 'y', 'z')
    )

    private fun getT9Permutations(digits: String): List<String> {""",
"""    private val t9Mapping = mapOf(
        '2' to listOf("a", "b", "c"),
        '3' to listOf("d", "e", "f"),
        '4' to listOf("g", "h", "i"),
        '5' to listOf("j", "k", "l"),
        '6' to listOf("m", "n", "o"),
        '7' to listOf("p", "q", "r", "s"),
        '8' to listOf("t", "u", "v"),
        '9' to listOf("w", "x", "y", "z")
    )

    private fun getT9Permutations(digits: String): List<String> {"""
)

with open('LovekeyIME/app/src/main/java/com/lovekey/ime/LovekeyIMEService.kt', 'w') as f:
    f.write(content)
