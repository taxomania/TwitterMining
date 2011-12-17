'''
@author: Tariq Patel
'''
from nltk import wordpunct_tokenize
import MySQLdb as mysql
from string import split
from pattern.en import polarity, singularize
from _mysql_exceptions import ProgrammingError

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
        self.db.query("SELECT id, text, sentiment FROM tweet ORDER BY id DESC LIMIT 10")
        return self.db.store_result()

    def hasEntry(self, word):
        self.db.query("SELECT t.type, d.id FROM dictionary d, dict_type t WHERE d.type = t.id AND d.software_name = '" + word + "'")
        self.soft = self.db.store_result().fetch_row()
        size = len(self.soft)
        if (size == 0):
            return False
        else:
            return True

    def getEntry(self):
        return self.soft[0]

    # This was just to test id worked
    def getSoftwareName(self, dict_id):
        self.db.query("SELECT software_name FROM dictionary WHERE id = '" + str(dict_id) + "'")
        return self.db.store_result().fetch_row()

    def close(self):
        self.db.close()

def tokenize(text):
    return wordpunct_tokenize(text)

# This function seems fairly inaccurate compared to Twitter Sentiment API
def analyse_sentiment(text):
    pol = polarity(text)

    if pol < -0.1:
        sentiment = "negative"
    elif pol > 0.1:
        sentiment = "positive"
    else:
        sentiment = "neutral"

    return sentiment

def singular(words):
    word_list = []
    for word in words:
        word_list.append(singularize(word))
    return word_list

if __name__ == '__main__':
    sql = SQLConnector()
    res = sql.load_data()

    for i in range(0, res.num_rows()):
        row = res.fetch_row()
        for tweet in row:
            tweet_id = tweet[0]
            text = tweet[1]
            words = tokenize(text)
            #print words
            # print singular(words)

            tagged_tweet = {}
            tagged_tweet['tweet_id'] = str(tweet_id)
            #tagged_tweet['tweet'] = text
# TODO: NEED TO BE ABLE TO TAG SOFTWARE WITH NAMES LONGER THAN 1 WORD eg iTunes Match - finds iTunes
            for word in words:
                try:
                    if sql.hasEntry(word):
                        entry = sql.getEntry()
                        #print entry
                        tagged_tweet['dict_id'] = str(entry[1])
                        tagged_tweet['name'] = word
                        tagged_tweet['type'] = entry[0]
                        #tagged_tweet[entry[0]] = word
                except ProgrammingError: # for error tokens
                    pass
                #if programming language stated
                    #tagged_tweet['programming_language']
                #if version stated
                    #tagged_tweet['version']
                #if price stated
                    #tagged_tweet['price']

            #reason for tweeting
            #tagged_tweet['reason']

            print tagged_tweet

    sql.close()
