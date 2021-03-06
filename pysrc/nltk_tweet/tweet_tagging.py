'''
@author: Tariq Patel
'''
import re
import sys

import argument_parser
from _mysql_exceptions import ProgrammingError
from nltk.tokenize import regexp_tokenize
from nltk.util import flatten

from bing import BingSearch
from database_connector import SQLConnector, MongoConnector
from pos_tagger import pos
from text_utils import *
from utils import Dictionary, IncompleteTaggingError

class NewSoftware(dict):
    def __init__(self, *args, **kwargs):
        dict.__init__(self, *args, **kwargs)

    def contains(self, software_name):
        return software_name in self

    def add(self, software_name, tweet):
        if self.contains(software_name):
            self[software_name]['tweets'] = flatten(self[software_name]['tweets'], tweet)
            self[software_name]['weight'] += 1
        else:
            self[software_name] = {'tweets':tweet, 'weight': 1}

    def remove(self, software_name):
        software_name = software_name.lower()
        if self.contains(software_name):
            del self[software_name]

def ngrams(tokens, max_n):
    ngrams = {}
    for n in range(max_n):
        candidates = []
        for i in range(len(tokens) - n):
            phrase = " ".join(tokens[i:i + n + 1])
            candidates.append(phrase)
        ngrams[n + 1] = candidates
    return ngrams

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
                        if check_bing(software,bing):
                            # Add newly-found software names to list, add to dictionary at end
                            new_software.add(software, tweet)
                            possible_tags.append(tweet_id)
                            #sql.insertSoftware(software) # This task now done at end
                    except ServerError, e:
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
                        if check_bing(software, bing):
                            # Add newly-found software names to list, add to dictionary at end
                            new_software.add(software, tweet)
                            possible_tags.append(tweet_id)
                    except ServerError, e:
                        print e
                        raise IncompleteTaggingError()

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
                    tweet.add('os_name', word)
                    tweet.add('os_id', str(entry[0]))
            except ProgrammingError: # for error tokens eg ' or "
                pass
            # Still need to deduce other reasons for tweeting eg review, notify others
            if word == 'release':
                tweet.add('reason', word)

    return tweet

def main():
    args = argument_parser.main()
    global sql
    sql = SQLConnector(host=args.host,
                       port=args.port,
                       user=args.user,
                       passwd=args.password,
                       db=args.db)
    global bing
    bing = BingSearch()
    global new_software
    new_software = NewSoftware()
    global possible_tags
    possible_tags = []
    mongo = MongoConnector(host=args.H, db=args.db)
    for page in range(1):
        res = sql.load_data(page)
        rows = res.num_rows()
        if not rows:
            print "No tweets left to analyse"
            break

        for _i_ in range(1):#rows):
            for tweet in res.fetch_row():
                tweet_id = str(tweet[0])
                text = tweet[1].lower()
                # text = "Version 2 Microsoft just released MS Office ver 3.20.2 for 99 cent 100c 10ps 13pence 10 pence"

                urls = find_url(text)
                for url in urls:
                    text = text.replace(url, "").strip()

                versions = find_version(text)

                words = regexp_tokenize(text, pattern=r'\w+([.,]\w+)*|\S+')
                #print words
                prices = find_price(words)

                pos_ = pos(words)
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
                        if (tagged_tweet.contains('software_id') or 
                        tagged_tweet.contains('operating_system_id')):
                            print tweet
                            print tagged_tweet
                            print
                            #mongo.insert(tagged_tweet)
                        else:
                            print tweet, "No software"
                        #sql.setTagged(tagged_tweet.get('tweet_db_id'))
                except IncompleteTaggingError, e:
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
