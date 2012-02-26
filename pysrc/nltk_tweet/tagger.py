'''
Created on Feb 24, 2012

@author: Tariq Patel
'''

import re
import sys

from nltk.tokenize import wordpunct_tokenize, regexp_tokenize
from nltk.util import flatten,ngrams

from database_connector import SQLConnector, MongoConnector
from pos_tagger import pos
from text_utils import *
from utils import Dictionary, IncompleteTaggingError

class TweetTagger(object):
    def __init__(self):
        super(TweetTagger, self).__init__()
        self.sql = SQLConnector()
        #self.mongo = MongoConnector()

    def _tag(self, tweet):
        tweet_id = str(tweet[0])
        text = tweet[1].lower()

        urls = find_url(text)
        for url in urls:
            text = text.replace(url, "").strip()

        versions = find_version(text)

        words = regexp_tokenize(text, pattern=r'\w+([.,]\w+)*|\S+')
        #print words
        prices = find_price(words)

        # MAIN TAGGING DONE HERE
        pos_ = pos(words)

        ngrams_ = self._ngrams(pos_, 2)

        tagged_tweet = self._tagger(ngrams_, tweet_id)
        tagged_tweet.add('sentiment', tweet[2])
        tagged_tweet.add('tweet', text)
        tagged_tweet.add('url', urls)
        tagged_tweet.add('version', versions)
        tagged_tweet.add('price', prices)
        return tagged_tweet

    def _tagger(self, ngram, tweet_id):
        return Dictionary()

    def _ngrams(self, pos_words, max_n):
        ngram = {}
        for n in range(1, max_n):
            ngram[n] = ngrams(pos_words, n)
        return ngram

    def tag(self, range_):
        for page in xrange(range_):
            res = self.sql.load_data(page)
            rows = res.num_rows()
            if not rows:
                print "No tweets left to analyse"
                break
            for _i_ in range(1):#rows):
                for tweet in res.fetch_row():
                    try:
                        tagged_tweet = self._tag(tweet)
                        # CHECK TAGS, ADD TO DB ETC HERE
                    except IncompleteTaggingError as e:
                        # Allow tagging again at a later stage
                        print tweet_id + ":", e
                        print tweet
                        print

    def close(self):
        self.sql.close()
        #self.mongo.close()

def main():
    tagger = TweetTagger()
    tagger.tag(2)
    tagger.close()
    return 0

if __name__ == "__main__":
    sys.exit(main())

