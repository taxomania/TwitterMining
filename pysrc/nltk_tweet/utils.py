'''
@author: Tariq Patel
'''
class Dictionary(dict):
    def __init__(self, *args, **kwargs):
        dict.__init__(self, *args, **kwargs)

    def contains(self, key):
        return key in self

    def add(self, key, value):
        if value is not None and len(value) > 0:
            if not self.contains(key):
                if len(value) == 1:
                    self[key] = value[0]
                else:
                    self[key] = value
            else:
                self[key] = flatten(self[key], value)

    def remove(self, key):
        del self[key]

class ServerError(Exception):
    def __init__(self, *args, **kwargs):
        Exception.__init__(self, *args, **kwargs)

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

