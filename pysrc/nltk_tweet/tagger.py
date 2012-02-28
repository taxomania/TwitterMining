'''
Created on Feb 24, 2012

@author: Tariq Patel
'''

import re
import sys

from _mysql_exceptions import ProgrammingError
from nltk.tokenize import wordpunct_tokenize, regexp_tokenize
from nltk.util import flatten,ngrams

from database_connector import SQLConnector, MongoConnector
from pos_tagger import pos
from text_utils import *
from utils import Dictionary, IncompleteTaggingError

class TweetTagger(object):
    def __init__(self):
        super(TweetTagger, self).__init__()
        self._sql = SQLConnector()
        #self._mongo = MongoConnector()

    def _tag(self, tweet):
        tweet_id = str(tweet[0])
        text = tweet[1].lower()
        #text = "download holiday havoc in itunes"

        urls = find_url(text)
        for url in urls:
            text = text.replace(url, "").strip()

        versions = find_version(text)

        words = regexp_tokenize(text, pattern=r'\w+([.,]\w+)*|\S+')
        #print words
        prices = find_price(words)

        pos_ = pos(words)

        #ngrams_ = self._ngrams(pos_, 2)
        n = 5
        five_gram = None
        while not five_gram:
            # robustness for when tweet length is less than n
            five_gram = ngrams(pos_, n)
            n -= 1
        #print five_gram

        tagged_tweet = self._ngram_tagger(five_gram, tweet_id)
        tagged_tweet.add('sentiment', tweet[2])
        tagged_tweet.add('tweet', text)
        tagged_tweet.add('url', urls)
        tagged_tweet.add('version', versions)
        tagged_tweet.add('price', prices)
        return tagged_tweet

    def _ngram_tagger(self, ngram, tweet_id):
        tags = Dictionary()
        tags.add('tweet_db_id', tweet_id)

        for tagged_words in ngram:
            self._tagger(tagged_words, tags)
        return tags

    def _tagger(self, gram, tags):
        words = []
        tags_ = []
        phrase = ""
        pos_soft = ""
        possible_software = False
        # Compile regular expressions outside of for loop
        # for efficiency purposes
        free_price = re.compile(r'free', re.IGNORECASE)
        check_is = re.compile(r'is', re.IGNORECASE)
        check_get = re.compile(r'download|get', re.IGNORECASE)
        for tagged_word in gram:
            word = tagged_word[0]
            tag = tagged_word[1]
            phrase += word + " "
            if possible_software:
                if tagIsNoun(tag):
                    pos_soft += word + " "
                    if word == gram[len(gram)-1][0]:
                        pos_soft = ""
                else:
                    possible_software = False
            if re.match(free_price, word):
                try:
                    prev = words.pop()
                    words.append(prev)
                    if re.match(check_is, prev):
                        tags.add('price', word)
                    else:
                        prev = tags_.pop()
                        tags_.append(prev)
                        if tagIsNoun(prev):
                            tags.add('price', word)
                except:
                    # This is first word in phrase
                    pass
            elif re.match(check_get, word):
                possible_software = True


            # Back in main part of loop
            words.append(word)
            tags_.append(tag)

        # End of for loop

        phrase = phrase.strip()
        if len(pos_soft) > 0:
            pos_soft = pos_soft.strip()
            tags.add('software_name', pos_soft)

        # CHECK DB HERE? OR ABOVE

    def tag(self, pages):
        for page in xrange(pages):
            res = self._sql.load_data(page)
            rows = res.num_rows()
            if not rows:
                print "No tweets left to analyse"
                break
            for _i_ in range(1):#rows):
                for tweet in res.fetch_row():
                    try:
                        tagged_tweet = self._tag(tweet)
                        print tagged_tweet
                        # CHECK TAGS, ADD TO DB ETC HERE
                    except IncompleteTaggingError as e:
                        # Allow tagging again at a later stage
                        print tweet_id + ":", e
                        print tweet
                        print

    def close(self):
        self._sql.close()
        #self._mongo.close()

def main():
    tagger = TweetTagger()
    tagger.tag(2)
    tagger.close()
    return 0

if __name__ == "__main__":
    sys.exit(main())

