import random
def accurate(n):
    intValue = 0.0
    decValue = 0.0
    accurateAns = 0.0
    n = n / 1.8

    if(n>110):
        decValue = random.random()
        intValue = random.randint(100,110)
        accurateAns = intValue + decValue
        return round(accurateAns,3)

    if(n<70):
        decValue = random.random()
        intValue = random.randint(70,80)
        accurateAns = intValue + decValue
        return round(accurateAns, 3)

    return round(n,3)

