'''
@author: Tariq Patel
'''
from nltk.tokenize import wordpunct_tokenize, regexp_tokenize
from pattern.en import polarity
from _mysql_exceptions import ProgrammingError
from DatabaseConnector import SQLConnector
import sys
import re

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

def check_version(word):
    regex = re.compile(pattern=r'(?:(\d+)\.)?(?:(\d+)\.)?(\*|\d+)')
    return re.match(regex, word)

# Doesn't match all prices
# Possible regexs to use:
#'^\$(\d*(\d\.?|\.\d{1,2}))$'
#'^\$\??\d{0,10}(\.\d{2})?$'
# call after tokenization
def find_price(text, pattern=r'^\$(\d*(\d\.?|\.\d{1,2}))$'):
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

def tag_tweets(ngrams):
    tweet = Dictionary()
    prev_is_software = False
    for i in range(len(ngrams), 0, -1):
        for word in ngrams[i]:
            if prev_is_software:
                if check_version(word):
                    tweet.add('version', word)
                prev_is_software = False
            # Look for 'Get x free'
            # TODO: Also look for 'Get x now' / 'Get x on' etc
            elif re.match(r'^[Gg][Ee][Tt][\w\s]*[Ff][Rr][Ee][Ee]$', word):
                software = word.replace(re.findall(re.compile(r'^[Gg][Ee][Tt]'), word)[0], "").strip()
                software = software.replace(
                                re.findall(re.compile(r'[Ff][Rr][Ee][Ee]$'), word)[0], "").strip()
                if not sql.isSoftware(software):
                    sql.insertSoftware(software)
                tweet.add('price', 'free')
            elif re.match(r'^\d+\s?(cents?|pence|[cp])+$', word):
                tweet.add('price', word)
            try:
                if sql.isSoftware(word):
                    entry = sql.getSoftware()
                    tweet.add('software_id', str(entry[0]))
                    tweet.add('software_name', word)
                    prev_is_software = True
                elif sql.isProgLang(word):
                    entry = sql.getProgLang()
                    tweet.add('programming_language_name', word)
                    tweet.add('programming_language_id', str(entry[0]))
                elif sql.isCompany(word):
                    entry = sql.getCompany()
                    tweet.add('company_name', word)
                    tweet.add('company_id', str(entry[0]))
                elif sql.isOS(word):
                    entry = sql.getOS()
                    tweet.add('operating_system_name', word)
                    tweet.add('operating_system_id', str(entry[0]))
            except ProgrammingError: # for error tokens eg ' or "
                pass
            # Still need to deduce other reasons for tweeting eg review, notify others
            if word == 'release':
                tweet.add('reason', word)

        #if license type stated eg BSD, APACHE
            #tagged_tweet['license']
    return tweet

class Dictionary(dict):
    def __init__(self, *args, **kwargs):
        dict.__init__(self, *args, **kwargs)

    def contains(self, key):
        return key in self

    def add(self, key, value):
        if value is not None:
            if not self.contains(key):
                self[key] = value
            else:
                obj = self[key]
                try: # Assume obj is a list
                    obj.append(value)
                    self[key] = obj
                except: # if obj is not a list
                    list_ = []
                    list_.append(obj)
                    list_.append(value)
                    self[key] = list_

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
    for page in range(0,1):
        res = sql.load_data(page)

        for _i_ in range(0, res.num_rows()):
            row = res.fetch_row()
            for tweet in row:
                tweet_id = str(tweet[0])
                text = tweet[1]
                # text = "Version 2 Microsoft just released MS Office ver 3.20.2 for 99 cent 100c 10ps 13pence 10 pence"

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
                tagged_tweet.add('tweet_db_id', tweet_id)
                tagged_tweet.add('sentiment', tweet[2])
                tagged_tweet.add_list('url', urls)
                tagged_tweet.add_list('version', versions)
                tagged_tweet.add_list('price', prices)

                # testing
                #if tagged_tweet.contains('software_id'):
                #   tagged_tweet.add('tweet', text)
                #if tagged_tweet.contains('price'):
                print tweet
                print tagged_tweet
                print
                #sql.insert(tagged_tweet)

    sql.close()
    return 0

if __name__ == '__main__':
    sys.exit(main())
