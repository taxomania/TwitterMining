'''
@author: Tariq Patel
'''
from nltk.tokenize import wordpunct_tokenize, regexp_tokenize
from pattern.en import polarity
from _mysql_exceptions import ProgrammingError
from DatabaseConnector import SQLConnector
import sys

def regex_tokenize(text, pattern):
    return regexp_tokenize(text, pattern)

# call before tokenization
def find_url(text, pattern=r'(http://[^ ]+)'):
    return regex_tokenize(text, pattern)

# call before tokenization
def find_version(text, pattern=None):
    digit_pattern = r'(?:(\d+)\.)?(?:(\d+)\.)?(\*|\d+)'
    pattern = '\s?[vV]ersion\s?'+ digit_pattern
    pattern += '| [vV]er\s?\.?\s?' + digit_pattern
    pattern += '| [vV]\s?\.?\s?' + digit_pattern
    version_matches = regex_tokenize(text, pattern)
    pattern = digit_pattern + '$'
    versions = []
    for version in version_matches:
        matches = regex_tokenize(version, pattern)
        for match in matches:
            versions.append(match)
    return versions

# Doesn't match all prices
# Possible regexs to use:
#'^\$(\d*(\d\.?|\.\d{1,2}))$'
#'^\$\??\d{0,10}(\.\d{2})?$'
# call after tokenization
def find_price(text, pattern=r'^\$(\d*(\d\.?|\.\d{1,2}))$'):
    import re
    pattern = re.compile(pattern)
    prices = []
    for word in text:
        if re.match(pattern, word):
            prices.append(word)
    return prices

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

def ngram(tokens, max_n):
    ngrams = {}
    for n in range(0, max_n):
        candidates = []
        for i in range(len(tokens) - n):
            phrase = " ".join(tokens[i:i + n + 1])
            candidates.append(phrase)
        ngrams[n + 1] = candidates
    return ngrams

def isKey(tuples, key):
    return key in tuples

def tags_found(tagged, tags):
    for tag in tags:
        if not isKey(tagged, tag):
            return False
    return True

def tag_tweets(words):
    # Assumes there is only one software etc per tweet
    sql_tags = ['software_name', 'programming_language_name', 'company_name']
    tweet = Dictionary()
    for i in range(len(words), 0, -1):
        for word in words[i]:
            if not tags_found(tweet, sql_tags):
                try:
                    if not isKey(tweet, 'software_name') and sql.isSoftware(word):
                        entry = sql.getSoftware()
                        tweet.add('software_id', str(entry[1]))
                        tweet.add('software_name', word)
                        tweet.add('software_type', entry[0])
                    elif (not isKey(tweet, 'programming_language_name')
                          and sql.isProgLang(word)):
                            entry = sql.getProgLang()
                            tweet.add('programming_language_name', word)
                            tweet.add('programming_language_id', str(entry[0]))
                    elif not isKey(tweet, 'company_name') and sql.isCompany(word):
                        entry = sql.getCompany()
                        tweet.add('company_name', word)
                        tweet.add('company_id', str(entry[0]))
                except ProgrammingError: # for error tokens eg ' or "
                    pass
        #if version stated
            #tagged_tweet['version_number']
        #if license type stated eg BSD, APACHE
            #tagged_tweet['license']

    #reason for tweeting
    #tagged_tweet['reason']
    return tweet

class Dictionary(dict):
    def __init__(self, *args, **kwargs):
        dict.__init__(self, *args, **kwargs)

    def contains(self, key):
        return key in self

    def add(self, key, value):
        if value is not None:
            self[key] = value

    def remove(self, key):
        del self[key]

    def add_list(self, key, array):
        if len(array) == 1:
            self.add(key, array[0])
        elif len(array) > 1:
            self.add(key, array)

def main():
    global sql
    sql = SQLConnector()
    res = sql.load_data()

    for i in range(0, res.num_rows()):
        row = res.fetch_row()
        for tweet in row:
            text = tweet[1]
            #text = "Version 2 Microsoft just released MS Office ver 3.20.2"

            urls = find_url(text)
            for url in urls:
                text = text.replace(url, "").strip()

            versions = find_version(text)

            words = regex_tokenize(text, pattern=r'\w+([.,]\w+)*|\S+')
            #print words
            prices = find_price(words)

            ngrams = ngram(words, 5)
            #for j in range(len(ngrams),0, -1):
                #print ngrams[j]

            tagged_tweet = tag_tweets(ngrams)
            tagged_tweet.add('tweet_db_id', str(tweet[0]))
            tagged_tweet.add('sentiment', tweet[2])
            tagged_tweet.add_list('url', urls)
            tagged_tweet.add_list('version', versions)
            tagged_tweet.add_list('price', prices)

            if tagged_tweet.contains("version"):
                tagged_tweet.add('tweet', text)
                print tagged_tweet

    sql.close()
    return 0

if __name__ == '__main__':
    sys.exit(main())
