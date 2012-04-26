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
    def __init__(self, sql=None, mongo=None, **kwargs):
        super(TweetTagger, self).__init__()
        if not sql:
            self._sql = SQLConnector(host=kwargs['host'],
                                     port=kwargs['port'],
                                     user=kwargs['user'],
                                     passwd=kwargs['password'],
                                     db=kwargs['db'])
        else:
            self._sql = sql
        if not mongo:
            self._mongo = MongoConnector(host=kwargs['H'], port=kwargs['mongoport'], db=kwargs['db'])
        else:
            self._mongo = mongo
        self._keyword = None
        self._bing = BingSearch()
        self._binged = Dictionary()

    def _tag(self, tweet):
        tweet_id = str(tweet[0])
        original = tweet[1].decode('utf-8', 'ignore')
        text = original.lower().replace('#','').strip()
        #text = "download 60 hundred pounds 72 million $800 billion pounds holiday havoc v2 in itunes for free 99"

        urls = find_url(original)
        for url in urls:
            text = text.replace(url.lower(), "").strip()

        word_freqs = word_frequencies(text)
        #print word_freqs

        versions = find_version(text)

        words = regexp_tokenize(text, pattern=r'\w+([.,]\w+)*|\S+')
        prices = find_price(words)

        five_gram = self._create_ngram(tokenized=words, gram_length=5)

        tagged_tweet = self._ngram_tagger(five_gram, tweet_id)
        tagged_tweet.add('sentiment', tweet[2])
        tagged_tweet.add('tweet', original)
        tagged_tweet.add('url', urls)
        tagged_tweet.add('version', versions)
        tagged_tweet.add('price', prices)

        if tagged_tweet.contains('software_name'):
            query = {'software_name':tagged_tweet.get('software_name')}
            words = {}
            for w in word_freqs:
                words['words.'+w] = word_freqs[w]
            #print query, words
            self._mongo.update_freqs(query,words)

        return tagged_tweet

    def _create_ngram(self, tokenized, gram_length):
        pos_ = pos(tokenized)
        #print pos_
        gram = None
        while not gram: # In case tweet length less than gram_length
            gram = ngrams(pos_, gram_length)
            gram_length -= 1
        return gram

    def _ngram_tagger(self, ngram, tweet_id):
        tags = Dictionary()
        tags.add('tweet_db_id', tweet_id)
        if self._keyword:
            keyword = self._keyword
            if self._sql.isSoftware(keyword):
                entry = self._sql.getSoftware()
                tags.add('software_name', keyword)
                tags.add('software_id', str(entry[0]))
            elif self._sql.isCompany(keyword):
                entry = self._sql.getCompany()
                tags.add('company_name', keyword)
                tags.add('company_id', str(entry[0]))
            elif self._sql.isOS(keyword):
                entry = self._sql.getOS()
                tags.add('os_name', keyword)
                tags.add('os_id', str(entry[0]))

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
            try:
                if tagIsNoun(tag):
                    if self._sql.isSoftware(word):
                        entry = self._sql.getSoftware()
                        try:
                            prev_tag = tags_.pop()
                            tags_.append(prev_tag)
                            if not tagIsDeterminantOrPreposition(prev_tag):
                                tags.add('software_name',word)
                                tags.add('software_id', str(entry[0]))
                        except:
                            possible_software = True
                    elif self._sql.isCompany(word):
                        entry = self._sql.getCompany()
                        try:
                            prev_tag = tags_.pop()
                            tags_.append(prev_tag)
                            if not tagIsDeterminantOrPreposition(prev_tag):
                                raise # Add to tags
                        except:
                            tags.add('company_name',word)
                            tags.add('company_id', str(entry[0]))
                    elif self._sql.isProgLang(word):
                        entry = self._sql.getProgLang()
                        try:
                            prev_tag = tags_.pop()
                            tags_.append(prev_tag)
                            if not tagIsDeterminantOrPreposition(prev_tag):
                                raise # Add to tags
                        except:
                            tags.add('programming_language_name', word)
                            tags.add('programming_language_id', str(entry[0]))

                if self._sql.isOS(word):
                    entry = self._sql.getOS()
                    try:
                        prev_tag = tags_.pop()
                        tags_.append(prev_tag)
                        prev = words.pop()
                        words.append(prev)
                        if not tagIsDeterminantOrPreposition(prev_tag) or re.match(check_on, prev):
                            tags.add('os_name', word)
                            tags.add('os_id', str(entry[0]))
                    except:
                        possible_software = True
            except ProgrammingError:
                pass

            if possible_software:
                if tagIsNoun(tag):
                    pos_soft += word + " "
                    if word == gram[len(gram)-1][0]: # If 'word' is last word in n-gram
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
                if self._binged.contains(pos_soft):
                    if self._binged.get(pos_soft):
                        tags.add('software_name', pos_soft)
                else:
                    try:
                        bool_ = check_bing(pos_soft, self._bing)              
                        self._binged[pos_soft]=  bool_
                        if bool_:
                            # Insert into dictionary db?
                            tags.add('software_name', pos_soft)
                    except ServerError, e:
                        print e
                    raise IncompleteTaggingError()

        # CHECK DB HERE? OR ABOVE

    def tag_by_tweet_id(self, tweet_id):
        return self._tag_tweet(tweet=self._sql.get_tweet(tweet_id), store=False)

    def _tag_tweet(self, tweet, store=True):
        try:
            tagged_tweet = self._tag(tweet)
            if store:
                id_ = tagged_tweet.get('tweet_db_id')
                if tagged_tweet.contains('company_name'):
                    self._mongo.insert(**tagged_tweet)
                if (tagged_tweet.contains('software_name') or tagged_tweet.contains('os_name')):
                    self._mongo.insert(**tagged_tweet)
                    self._sql.setFound(id_)
                else:
                    self._sql.setNotFound(id_)
                    # CHECK TAGS, ADD TO DB ETC HERE
                self._sql.setTagged(id_)
            return tagged_tweet
        except IncompleteTaggingError, e:
            # Allow tagging again at a later stage
            print tweet, e
            print
            return None

    def tag(self, pages=100, store=True, keyword=None):
        total_tags = []
        self._keyword=keyword if keyword else None
        for page in xrange(pages):
            if keyword:
                res = self._sql.load_data(max_results=30, keyword=keyword)
            else:
                res = self._sql.load_data(max_results=350)
            rows = res.num_rows()
            if not rows:
                print "No tweets left to analyse"
                break
            for _i_ in range(rows):
                for tweet in res.fetch_row():
                    try:
                        total_tags.append(self._tag_tweet(tweet=tweet, store=store))
                    except:
                        continue
        return total_tags

    def close(self):
        self._sql.close()
        self._mongo.close()

def main(args):
    args = vars(args)
    tagger = TweetTagger(**args)
    tagger.tag(2)
    tagger.close()
    return 0

if __name__ == "__main__":
    sys.exit(main(argument_parser.main()))

