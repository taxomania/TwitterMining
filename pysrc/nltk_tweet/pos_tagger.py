'''
Created on Feb 7, 2012

@author: Tariq Patel
'''

import re
import sys
from multiprocessing import Process

import nltk

from database_connector import SQLConnector
from tweet_tagging import find_url

def trim(words):   
    for word in words:
        if re.match(re.compile(r'[.,!"]'),word):
            words.remove(word)
    return words

# This is to allow pos() function to be called from elsewhere
def pre_process(tweet):
    tweet_id = str(tweet[0])
    text = tweet[1]

    urls = find_url(text)
    for url in urls:
        text = text.replace(url,"").strip()

    print text  #testing
    print

    words = trim(nltk.word_tokenize(text))
    print words #testing
    print

    return words

def pos(words):
    tagged_words = nltk.pos_tag(words)
    pos = []

    for tags in tagged_words:
        if not re.match(re.compile(r'[.,:]'), tags[1]):
            pos.append(tags)
    print pos
    print
    return pos

def task(res, rows):
    for _i_ in range(0,2):#rows):
        row = res.fetch_row()
        for tweet in row:
            pos(pre_process(tweet))

def pos_all(sql):
    queue =[]
    for page in range(0,3):
        res = sql.load_data(page)
        rows = res.num_rows()
        if not rows:
            print "No tweets left to analyse"
            break
            
        thread = Process(name=str(page), target=task, args=(res,rows))
        thread.start()
        queue.append(thread)

    for t in queue:
        t.join()

def pos_tweet(sql, tweet_id):
    tweet = sql.get_tweet(tweet_id)
    if not tweet:
        print "Tweet does not exist"
    else:
        pos(pre_process(tweet))

def main():
    sql = SQLConnector()
    if len(sys.argv) == 1:
        pos_all(sql)
    else:
        args = sys.argv[1:]
        for arg in args:
            pos_tweet(sql, arg)
    sql.close()
    return 0

if __name__ == "__main__":
    sys.exit(main())

