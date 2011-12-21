'''
@author: Tariq Patel
'''
from nltk.tokenize import wordpunct_tokenize
from pattern.en import polarity, singularize
from _mysql_exceptions import ProgrammingError
from DatabaseConnector import SQLConnector

def tokenize(text):
    return wordpunct_tokenize(text)

# This function seems fairly inaccurate compared to Twitter Sentiment API
def analyse_sentiment(text):
    pol = polarity(text)

    if pol < -0.1:
        sentiment = "negative"
    elif pol > 0.1:
        sentiment = "positive"
    else:
        sentiment = "neutral"

    return sentiment

def singular(words):
    word_list = []
    for word in words:
        word_list.append(singularize(word))
    return word_list

def isKey(tuples, key):
    return key in tuples


def ngram(tokens, max_n):
    ngrams = {}
    for n in range(0, max_n-1):
        candidates = []
        for i in range(len(tokens) - n):
            phrase = " ".join(tokens[i:i + n + 1])
            candidates.append(phrase)
        ngrams[n+1] = candidates
    return ngrams

if __name__ == '__main__':
    sql = SQLConnector()
    res = sql.load_data()

    for i in range(0, 1):#res.num_rows()):
        row = res.fetch_row()
        for tweet in row:
            text = tweet[1]

            # Need improved tokenizer to take hyperlinks
            words = tokenize(text)

            ngram_search = ngram(words, 6)

            # print all ngrams
            for i in range(len(ngram_search),0, -1):
                print ngram_search[i]

            tagged_tweet = {}
            tagged_tweet['tweet_id'] = str(tweet[0])
            tagged_tweet['sentiment'] = tweet[2]
            #tagged_tweet['tweet'] = text
# TODO: NEED TO BE ABLE TO TAG SOFTWARE WITH NAMES LONGER THAN 1 WORD eg iTunes Match - finds iTunes
            for word in words:
                try:
                    if sql.isSoftware(word):
                        entry = sql.getSoftware()
                        tagged_tweet['dict_id'] = str(entry[1])
                        tagged_tweet['software_name'] = word
                        tagged_tweet['type'] = entry[0]
                    elif sql.isProgLang(word):
                        entry = sql.getProgLang()
                        tagged_tweet['programming_language'] = str(entry[0])
                    elif sql.isCompany(word):
                        entry = sql.getCompany()
                        tagged_tweet['company'] = str(entry[0])
                except ProgrammingError: # for error tokens eg '
                    pass
                #if version stated
                    #tagged_tweet['version_number']
                #if license type stated eg BSD, APACHE
                    #tagged_tweet['license']
                #if price stated
                    #tagged_tweet['price']

            #reason for tweeting
            #tagged_tweet['reason']

            #if isKey(tagged_tweet, 'company'): # Using this for testing tags
            print tagged_tweet

            sql.close()
