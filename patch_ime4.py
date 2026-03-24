with open('LovekeyIME/app/src/main/java/com/lovekey/ime/LovekeyIMEService.kt', 'r') as f:
    content = f.read()

# Fix the compiler error about unused variable or syntax
content = content.replace("val candidates = mutableListOf<String>()", "")

with open('LovekeyIME/app/src/main/java/com/lovekey/ime/LovekeyIMEService.kt', 'w') as f:
    f.write(content)
