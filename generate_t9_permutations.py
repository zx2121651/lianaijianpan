import sys
# Create a python script to test T9 logic
t9_mapping = {
    '2': 'abc', '3': 'def',
    '4': 'ghi', '5': 'jkl', '6': 'mno',
    '7': 'pqrs', '8': 'tuv', '9': 'wxyz'
}

def get_permutations(digits):
    if not digits:
        return []
    res = ['']
    for d in digits:
        if d not in t9_mapping:
            continue
        new_res = []
        for ch in t9_mapping[d]:
            for prefix in res:
                new_res.append(prefix + ch)
        res = new_res
    return res

print(get_permutations('22'))
