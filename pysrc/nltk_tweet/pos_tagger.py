'''
Created on Feb 7, 2012

@author: Tariq Patel
'''

import re
import sys
from multiprocessing import Process, Queue

import nltk

from database_connector import SQLConnector
from tweet_tagging import find_url

def trim(words):   
    for word in words:
        if re.match(re.compile(r'[.,!]'),word):
            words.remove(word)
    return words

def task(res, rows):
    for _i_ in range(0,rows):
        row = res.fetch_row()
        for tweet in row:
            tweet_id = str(tweet[0])
            text = tweet[1]
            
            urls = find_url(text)
            for url in urls:
                text = text.replace(url,"").strip()
            #print text  #testing
            #print

            words = trim(nltk.word_tokenize(text))
            #print words #testing
            #print

            pos = nltk.pos_tag(words)
            #print pos
            #print

def main():
    sql = SQLConnector()
    queue =[]
    for page in range(0,3):
        res = sql.load_data(page)
        rows = res.num_rows()
        if rows == 0:
	        #print "No tweets left to analyse"
			break
	
        thread = Process(name=str(page), target=task, args=(res,rows))
        thread.start()
        queue.append(thread)
    '''
    POS TAGGING OUTCOMES
    JJ  adjective                   new, good, high, special, big, local
    RB  adverb                      really, already, still, early, now, again
    CC  coordinating conjunction    and, or, but, if, while, although
    DT  determiner                  the, this, a, some, most, every, no
    EX  existential                 there, there's
    FW  foreign word                dolce, ersatz, esprit, quo, maitre
    MOD modal verb                  will, can, would, may, must, should
    NN  noun                        year, home, costs, time, education
    NP  proper noun                 Alison, Africa, April, Washington
    NUM number                      twenty-four, fourth, 1991, 14:24
    PRP pronoun                     he, their, her, its, my, I, us
    IN  preposition                 on, of, at, with, by, into, under
    TO  the word to                 to
    UH  interjection                ah, bang, ha, whee, hmpf, oops
    V   verb                        is, has, get, do, make, see, run
    VD  past tense                  said, took, told, made, asked
    VBP present participle          making, going, playing, working
    VN  past participle             given, taken, begun, sung
    WH  wh determiner               who, which, when, what, where, how   
    VBD simple past                 went
    VBZ 3rd singular present        goes
    RP
    PRP$
    VB  base verb                   go
    NNS
    CD
    NNP
    VBG gerund                      going

    '''
    for t in queue:
        t.join()
    sql.close()
    return 0

if __name__ == "__main__":
    sys.exit(main())
