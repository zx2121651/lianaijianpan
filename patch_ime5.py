with open('LovekeyIME/app/src/main/java/com/lovekey/ime/LovekeyIMEService.kt', 'r') as f:
    content = f.read()

# Add the variable initialization back
old_block = """                    if (numPredicts > 0) {

                        val maxCandidates = minOf(numPredicts, 5) // Take top 5 from each valid permutation"""
new_block = """                    if (numPredicts > 0) {
                        val candidates = mutableListOf<String>()
                        val maxCandidates = minOf(numPredicts, 5) // Take top 5 from each valid permutation"""

content = content.replace(old_block, new_block)

with open('LovekeyIME/app/src/main/java/com/lovekey/ime/LovekeyIMEService.kt', 'w') as f:
    f.write(content)
