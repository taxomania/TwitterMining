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

        pos_ = pos(words)

        #ngrams_ = self._ngrams(pos_, 2)
        five_gram = ngrams(pos_, 5)
        print five_gram

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
            self._gram_tagger(tagged_words)

        return tags

    def _gram_tagger(self, gram):
        words = []
        tags = []
        phrase = ""
        for tagged_word in gram:
            word = tagged_word[0]
            words.append(word)
            tags.append(tagged_word[1])
            phrase += word + " "
        phrase = phrase.strip()
        print words
        print tags
        print phrase



    def _ngrams_tagger(self, ngrams_, tweet_id):
        tags = Dictionary()
        tags.add('tweet_db_id', tweet_id)

        for i in range(len(ngrams_), 0, -1):
            for words in ngrams_[i]:
                self._tagger(words, tags)
        return tags

    def _tagger(self, ngram, tweet):
        word= ""
        tags = []
        for pos in ngram:
            tags.append(pos[1])
            word += pos[0] +" "
        word = word.strip()
        # START TAGGING HERE
        print word
        print tags

    def _ngrams(self, pos_words, max_n):
        ngram = {}
        for n in range(1, max_n+1):
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
                        print tagged_tweet
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

