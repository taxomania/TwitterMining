'''
@author: Tariq Patel
'''
from nltk.tokenize import wordpunct_tokenize, regexp_tokenize
from pattern.en import polarity, singularize
from _mysql_exceptions import ProgrammingError
from DatabaseConnector import SQLConnector
import sys

def find_url(text):
    return regexp_tokenize(text, pattern=r"(http://[^ ]+)")

def regex_tokenize(text, pattern):
    return regexp_tokenize(text, pattern)

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

def tag_tweets(words):
# TODO: NEED TO BE ABLE TO TAG SOFTWARE WITH NAMES LONGER THAN 1 WORD eg iTunes Match - finds iTunes
    tagged_tweet = {}
    for worda in range(len(words),0, -1):
        for word in words[worda]:
            try:
                if not isKey(tagged_tweet, 'software_name') and sql.isSoftware(word):
                    entry = sql.getSoftware()
                    tagged_tweet['software_id'] = str(entry[1])
                    tagged_tweet['software_name'] = word
                    tagged_tweet['type'] = entry[0]
                elif not isKey(tagged_tweet, 'programming_language_name') and sql.isProgLang(word):
                    entry = sql.getProgLang()
                    tagged_tweet['programming_language_name'] = word
                    tagged_tweet['programming_language_id'] = str(entry[0])
                elif not isKey(tagged_tweet, 'company_name') and sql.isCompany(word):
                    entry = sql.getCompany()
                    tagged_tweet['company_name'] = word
                    tagged_tweet['company_id'] = str(entry[0])
            except ProgrammingError: # for error tokens eg ' or "
                pass

        #if version stated
            #tagged_tweet['version_number']
        #if license type stated eg BSD, APACHE
            #tagged_tweet['license']
        #if price stated
            #tagged_tweet['price']

    #reason for tweeting
    #tagged_tweet['reason']
    return tagged_tweet

def main():
    global sql
    sql = SQLConnector()
    res = sql.load_data()

    for i in range(0, res.num_rows()):
        row = res.fetch_row()
        for tweet in row:
            text = tweet[1]

            urls = find_url(text)
            for url in urls:
                text = text.replace(url, "").strip()

            words = regex_tokenize(text, pattern=r'\w+([.,]\w+)*|\S+')
            print words

            ngrams = ngram(words, 6)
            #for i in range(len(ngrams),0, -1):
            #    print ngrams[i]

            tagged_tweet = tag_tweets(ngrams)
            tagged_tweet['tweet_db_id'] = str(tweet[0])
            tagged_tweet['sentiment'] = tweet[2]
            if len(urls) == 1:
                tagged_tweet['url'] = urls[0]
            elif len(urls) == 0:
                tagged_tweet['url'] = None
            else:
                tagged_tweet['url'] =  urls
            #tagged_tweet['tweet'] = text


            #if isKey(tagged_tweet, 'company'): # Using this for testing tags
            print tagged_tweet

    sql.close()
    return 0

if __name__ == '__main__':
    sys.exit(main())
