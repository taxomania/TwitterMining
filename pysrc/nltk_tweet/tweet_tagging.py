'''
@author: Tariq Patel
'''
import re
import sys

from _mysql_exceptions import ProgrammingError
from httplib2 import ServerNotFoundError
from nltk.tokenize import wordpunct_tokenize, regexp_tokenize
from nltk.util import flatten
from pattern.en import polarity

from bing import BingSearch
from database_connector import SQLConnector, MongoConnector

# These exception classes are used to differentiate between errors, but have no extra functionality
class ServerError(Exception):
    def __init__(self, *args, **kwargs):
        Exception.__init__(self, *args, **kwargs)

class IncompleteTaggingError(Exception):
    def __init__(self, *args, **kwargs):
        Exception.__init__(self, *args, **kwargs)

    def __str__(self, *args, **kwargs):
        return "Tweet tagging incomplete"

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

class NewSoftware(dict):
    def __init__(self, *args, **kwargs):
        dict.__init__(self, *args, **kwargs)

    def contains(self, software_name):
        return software_name in self

    def add(self, software_name, tweet):
        software_name = software_name.lower()
        if self.contains(software_name):
            self[software_name]['tweets'] = flatten(self[software_name]['tweets'], tweet)
            self[software_name]['weight'] = self[software_name]['weight'] + 1
        else:
            self[software_name] = {'tweets':tweet, 'weight': 1}

    def remove(self, software_name):
        software_name = software_name.lower()
        if self.contains(software_name):
            del self[software_name]

def regex_tokenize(text, pattern):
    return regexp_tokenize(text, pattern)

# call before tokenization
def find_url(text, pattern=r'(http://[^ ]+)'):
    return regex_tokenize(text, pattern)

# call before tokenization
def find_version(text, pattern=None):
    digit_pattern = r'(?:(\d+)\.)?(?:(\d+)\.)?(\*|\d+)'
    pattern = '\s?[vV]ersion\s?' + digit_pattern
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

def ngrams(tokens, max_n):
    ngrams = {}
    for n in range(0, max_n):
        candidates = []
        for i in range(len(tokens) - n):
            phrase = " ".join(tokens[i:i + n + 1])
            candidates.append(phrase)
        ngrams[n + 1] = candidates
    return ngrams

# This function is still slightly inaccurate
def check_bing(name):
    try:
        music = len(bing.search("\"" + name + " music \"")) # STUB
        movie = len(bing.search("\"" + name + " movie\"")) # STUB
        response = bing.search("\"" + name + "\"" + " software game") # STUB - BUGGY
        # print response
        size = len(response)
        if size > music and size > movie:
            results = response.get_results() # len(results) always 15
            total = 0
            pattern = '[Aa][Pp][Pp]'
            #pattern += '| [Gg][Aa][Mm][Ee]' # Not yet working
            regex = re.compile(pattern)
            for result in results:
                string = result['Title'] + " " + result['Description']
                # CAN USE `string` to find company name as well POTENTIALLY
                size = len(re.findall(regex, string)) # STUB
                if size > 0:
                    print string # STUB
                    total += size
            if total > 0:
                return True
            else:
                return False
    except ServerNotFoundError:
        raise ServerError("Could not connect to Bing")

def tag_tweets(ngrams, tweet_id):
    tweet = Dictionary()
    tweet.add('tweet_db_id', tweet_id)
    prev_is_software = False
    for i in range(len(ngrams), 0, -1):
        for word in ngrams[i]:
            if prev_is_software:
                if check_version(word):
                    tweet.add('version', word)
                prev_is_software = False
            # Look for 'Get x free'
            # This doesn't always work, eg 'get your free ...' / 'get it free'
            # TODO: Also look for 'Get x on' etc
            # Also look for 'Download x now' etc
            elif re.match(r'^[Gg][Ee][Tt][\w.\s]*[Ff][Rr][Ee][Ee]$', word):
                software = word.replace(re.findall(re.compile(r'^[Gg][Ee][Tt]'), word)[0], "").strip()
                software = software.replace(
                                re.findall(re.compile(r'[Ff][Rr][Ee][Ee]$'), word)[0], "").strip()
                if not sql.isSoftware(software):
                    try:
                        if check_bing(software):
                            # Add newly-found software names to list, add to dictionary at end
                            new_software.add(software, tweet)
                            possible_tags.append(tweet_id)
                            #sql.insertSoftware(software) # This task now done at end
                    except ServerError as e:
                        print e
                        raise IncompleteTaggingError()
                tweet.add('price', 'free')

            # REQUIRES REFACTORING
            elif re.match(r'^[Gg][Ee][Tt][\w.\s]*[Nn][Oo][Ww]$', word):
                software = word.replace(re.findall(re.compile(r'^[Gg][Ee][Tt]'), word)[0], "").strip()
                software = software.replace(
                                re.findall(re.compile(r'[Nn][Oo][Ww]$'), word)[0], "").strip()
                if not sql.isSoftware(software):
                    try:
                        if check_bing(software):
                            # Add newly-found software names to list, add to dictionary at end
                            new_software.add(software, tweet)
                            possible_tags.append(tweet_id)
                    except ServerError as e:
                        print e
                        raise IncompleteTaggingError()

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

    return tweet

def main():
    global sql
    sql = SQLConnector()
    global bing
    bing = BingSearch()
    global new_software
    new_software = NewSoftware()
    global possible_tags
    possible_tags = []
    mongo = MongoConnector()
    for page in range(0, 2):
        res = sql.load_data(page)
        rows = res.num_rows()
        if rows == 0:
            print "No tweets left to analyse"
            break

        for _i_ in range(0, rows):
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

                ngram = ngrams(words, 5)

                try:
                    tagged_tweet = tag_tweets(ngram, tweet_id)
                    tagged_tweet.add('tweet_text', text)
                    tagged_tweet.add('sentiment', tweet[2])
                    tagged_tweet.add('url', urls)
                    tagged_tweet.add('version', versions)
                    tagged_tweet.add('price', prices)
                    if tweet_id in possible_tags:
                        print tweet_id
                    else:
                        if tagged_tweet.contains('software_id'):
                            print tweet
                            print tagged_tweet
                            print
                            mongo.insert(tagged_tweet)
                        else:
                            print tweet, "No software"
                        sql.setTagged(tagged_tweet.get('tweet_db_id'))
                except IncompleteTaggingError as e:
                    # This will allow the tweet to be tagged again at a later stage
                    print tweet_id + ":", e
                    print tweet
                    print
    print
    #print new_software # evaluation purposes
    for software in new_software:
        print software, new_software.get(software)
    print possible_tags
    # TODO: Add new_software to dictionary, need to automate this process
    mongo.close()
    sql.close()
    return 0

if __name__ == '__main__':
    sys.exit(main())
