'''
Created on 24 Feb 2012

@author: Tariq Patel
'''

import re
import sys

from nltk.tokenize import wordpunct_tokenize, regexp_tokenize
from nltk.util import flatten

from database_connector import SQLConnector, MongoConnector
from pos_tagger import pos
from utils import Dictionary

class TweetTagger(object):
    def __init__(self):
        super(TweetTagger, self).__init__()
        self.sql = SQLConnector()
        self.mongo = MongoConnector()

    def _tag(self, row):
        for tweet in row:

    def tag(self, range_):
        for page in xrange(range_):
            res = self.sql.load_data(page)
            rows = res.num_rows()
            if not rows:
                print "No tweets left to analyse"
                break
        for _i_ in range(0, rows):
            _tag(res.fetch_row())
    
    def close(self):
        self.sql.close()
        self.mongo.close()

def main():
    tagger = TweetTagger()
    tagger.tag(2)
    tagger.close()
    return 0

if __name__ == "__main__":
    sys.exit(main())

