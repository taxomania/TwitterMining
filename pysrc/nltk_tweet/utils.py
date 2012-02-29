'''
@author: Tariq Patel
'''
from nltk.util import flatten
from pattern.en import polarity

class Dictionary(dict):
    def __init__(self, *args, **kwargs):
        dict.__init__(self, *args, **kwargs)

    def contains(self, key):
        return key in self

    def get(self, key):
        if self.contains(key):
            return self[key]
        else:
            return None

    def add(self, key, value):
        if value is not None and len(value) > 0:
            if not self.contains(key):
                if len(value) == 1:
                    self[key] = value[0]
                else:
                    self[key] = value
            else:
                if not self[key] == value:
                    self[key] = flatten(self[key], value)

    def remove(self, key):
        del self[key]

class IncompleteTaggingError(Exception):
    def __init__(self, *args, **kwargs):
        Exception.__init__(self, *args, **kwargs)

    def __str__(self, *args, **kwargs):
        return "Tweet tagging incomplete"

# This function seems fairly inaccurate compared to  
# Twitter Sentiment API
def analyse_sentiment(text):
    pol = polarity(text)
    if pol < -0.1:
        sentiment = "negative"
    elif pol > 0.1:
        sentiment = "positive"
    else:
        sentiment = "neutral"
    return sentiment

