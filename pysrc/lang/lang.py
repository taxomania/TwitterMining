from guess_language import guessLanguageName

def getLanguage(text):
    try:
        return guessLanguageName(text)
    except:
        return "Unknown"
