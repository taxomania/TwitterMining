'''
Created on Dec 16, 2011

@author: Tariq Patel
'''
from nltk import wordpunct_tokenize
import MySQLdb as mysql
from string import split

class SQLConnector:
    __userpass_retrieved = False
    __user = None
    __pass = None

    @staticmethod
    def getDetails():
        fin = open("/Users/Tariq/Documents/Eclipse/workspace/TwitterMining/sqluserpass.txt")
        strings = split(fin.readline(), ":")
        SQLConnector.__user = strings[0]
        SQLConnector.__pass = strings[1]
        fin.close()
        SQLConnector.__userpass_retrieved = True

    def __init__(self):
        if (not SQLConnector.__userpass_retrieved):
            SQLConnector.getDetails()
        self.db = mysql.connect(host='localhost',
                               user=SQLConnector.__user,
                               passwd=SQLConnector.__pass,
                               db='TwitterMining')

    def load_data(self):
        self.db.query("SELECT id, text FROM tweet ORDER BY id DESC LIMIT 10")
        self.result_set = self.db.store_result()

def tokenize_tweet(tweet):
    return wordpunct_tokenize(tweet)


if __name__ == '__main__':
    sql = SQLConnector()
    sql.load_data()
    res = sql.result_set

    for i in range(0, res.num_rows()):
        row = res.fetch_row()
        for tweet in row:
            tweet_id = tweet[0]
            text = tweet[1]
            words = tokenize_tweet(text)
            print words
            #for word in words:
            #   print word


