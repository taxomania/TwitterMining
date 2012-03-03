'''
Created on Feb 24, 2012

@author: Tariq Patel
'''

import re
import sys

from _mysql_exceptions import ProgrammingError
from nltk.tokenize import wordpunct_tokenize, regexp_tokenize
from nltk.util import flatten,ngrams

import argument_parser
from bing import BingSearch
from database_connector import SQLConnector, MongoConnector
from pos_tagger import pos
from text_utils import *
from utils import Dictionary, IncompleteTaggingError

class TweetTagger(object):
    def __init__(self, args):
        super(TweetTagger, self).__init__()
        self._sql = SQLConnector(args.host, args.port, args.user, args.password, args.d)
        #self._mongo = MongoConnector()
        self._bing = BingSearch()

    def _tag(self, tweet):
        tweet_id = str(tweet[0])
        text = tweet[1].lower()
        #text = "download 60 hundred pounds 72 million $800 billion pounds holiday havoc v2 in itunes for free 99"

        urls = find_url(text)
        for url in urls:
            text = text.replace(url, "").strip()

        versions = find_version(text)

        words = regexp_tokenize(text, pattern=r'\w+([.,]\w+)*|\S+')
        #print words
        prices = find_price(words)

        pos_ = pos(words)
        #print pos_

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
        free_price = re.compile(r'^free$', re.IGNORECASE)
        check_is = re.compile(r'^is$|^for$', re.IGNORECASE)
        check_get = re.compile(r'^download$|^get$', re.IGNORECASE)
        check_on = re.compile(r'^on$|^for$', re.IGNORECASE)
        for tagged_word in gram:
            word = tagged_word[0]
            tag = tagged_word[1]
            phrase += word + " "
            #print word, tag
            if tagIsNoun(tag):
                if self._sql.isSoftware(word):
                    entry = self._sql.getSoftware()
                    try:
                        prev_tag = tags_.pop()
                        tags_.append(prev_tag)
                        if not tagIsDeterminantOrPreposition(prev_tag):
                            raise # Add to tags
                    except:
                        tags.add('software_name', str(entry[1]))
                        tags.add('software_id', str(entry[0]))
                elif self._sql.isCompany(word):
                    entry = self._sql.getCompany()
                    try:
                        prev_tag = tags_.pop()
                        tags_.append(prev_tag)
                        if not tagIsDeterminantOrPreposition(prev_tag):
                            raise # Add to tags
                    except:
                        tags.add('company_name', str(entry[1]))
                        tags.add('company_id', str(entry[0]))
                elif self._sql.isOS(word):
                    entry = self._sql.getOS()
                    try:
                        prev_tag = tags_.pop()
                        tags_.append(prev_tag)
                        prev = words.pop()
                        words.append(prev)
                        if not tagIsDeterminantOrPreposition(prev_tag) or re.match(check_on, prev):
                            raise # Add to tags
                    except:
                        tags.add('os_name', str(entry[1]))
                        tags.add('os_id', str(entry[0]))
                elif self._sql.isProgLang(word):
                    entry = self._sql.getProgLang()
                    try:
                        prev_tag = tags_.pop()
                        tags_.append(prev_tag)
                        if not tagIsDeterminantOrPreposition(prev_tag):
                            raise # Add to tags
                    except:
                        tags.add('programming_language_name', str(entry[1]))
                        tags.add('programming_language__id', str(entry[0]))



            if possible_software:
                if tagIsNoun(tag):
                    pos_soft += word + " "
                    if word == gram[len(gram)-1][0]:
                        pos_soft = ""
                else:
                    prev = words.pop()
                    words.append(prev)
                    if not re.match(check_get, prev):
                        if check_version(word):
                            tags.add('version', word)
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
            if not tags.get('software_name'):
                try:
                    if check_bing(pos_soft, self._bing):
                        # Insert into dictionary db?
                        tags.add('software_name', pos_soft)
                except ServerError, e:
                    print e
                    raise IncompleteTaggingError()
        # CHECK DB HERE? OR ABOVE

    def tag(self, pages):
        for page in xrange(pages):
            res = self._sql.load_data(page)
            rows = res.num_rows()
            if not rows:
                print "No tweets left to analyse"
                break
            for _i_ in range(6):#rows):
                for tweet in res.fetch_row():
                    try:
                        tagged_tweet = self._tag(tweet)
                        print tagged_tweet
                        # CHECK TAGS, ADD TO DB ETC HERE
                    except IncompleteTaggingError, e:
                        # Allow tagging again at a later stage
                        print tagged_tweet.get('tweet_db_id') , ":", e
                        print tweet
                        print

    def close(self):
        self._sql.close()
        #self._mongo.close()

def main(args):
    tagger = TweetTagger(args)
    tagger.tag(2)
    tagger.close()
    return 0

if __name__ == "__main__":
    sys.exit(main(argument_parser.main()))

