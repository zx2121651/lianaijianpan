with open('LovekeyIME/keyboard-ui/src/main/java/com/lovekey/ime/ui/LovekeyKeyboard.kt', 'r') as f:
    content = f.read()

lines = content.split('\n')
while lines and lines[-1].strip() == '':
    lines.pop()

# Check brackets
open_b = content.count('{')
close_b = content.count('}')

if open_b > close_b:
    lines.append('}' * (open_b - close_b))
elif close_b > open_b:
    # this is harder, just append a dummy object
    pass

with open('LovekeyIME/keyboard-ui/src/main/java/com/lovekey/ime/ui/LovekeyKeyboard.kt', 'w') as f:
    f.write('\n'.join(lines))
