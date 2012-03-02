'''
Created on Dec 18, 2011

@author: Tariq Patel
'''

from string import split

import MySQLdb as sql
import pymongo

class SQLConnector(object):

    def __init__(self, host, port, user, passwd, db):
        self.db = sql.connect(host=host,
                              port=port,
                              user=user,
                              passwd=passwd,
                              db=db)

    def load_data(self, page=0, max_results=100):
        self.db.query("SELECT id, text, sentiment FROM tweet WHERE keyword='latest' "
                      + "AND tagged=FALSE ORDER BY id DESC LIMIT "
                      + str(page * max_results) + ', ' + str(max_results))
        return self.db.store_result()

    def get_tweet(self, tweet_id):
        self.db.query("SELECT id, text FROM tweet WHERE id='" + tweet_id + "'")
        tweet_tuple = self.db.store_result().fetch_row()
        if not len(tweet_tuple):
            return None
        else:
            return tweet_tuple[0]

    def isSoftware(self, word):
        return self.__isEntry("SELECT d.id, d.software_name FROM dictionary d, dict_type t "
                              +"WHERE d.type = t.id AND d.software_name = '" + word + "'")

    def getSoftware(self):
        return self.__getEntry()

    def insertSoftware(self, name):
        c = self.db.cursor()
        c.execute("INSERT INTO dictionary(software_name) VALUES('"+name+"')")
        id_ = self.db.insert_id()
        c.close()
        self.db.commit()
        return id_

    def __getEntry(self):
        return self.result[0]

    def __isEntry(self, query):
        self.db.query(query)
        self.result = self.db.store_result().fetch_row()
        size = len(self.result)
        if (size == 0):
            return False
        else:
            return True

    def isProgLang(self, word):
        return self.__isEntry("SELECT id, language FROM prog_lang WHERE language = '" + word + "'")

    def getProgLang(self):
        return self.__getEntry()

    def isOS(self, name):
        return self.__isEntry("SELECT id, os FROM os WHERE os = '" + name + "'")

    def getOS(self):
        return self.__getEntry()

    def isCompany(self, word):
        return self.__isEntry("SELECT id, name FROM company WHERE name = '" + word + "'")

    def getCompany(self):
        return self.__getEntry()

    def deleteUsersNoTweets(self):
        self.db.query("DELETE FROM user WHERE NOT EXISTS (SELECT NULL FROM tweet "
                      + "WHERE user.id=tweet.user_id)")
        self.db.commit()
        print "Deleted users with no associated tweets"

    def setTagged(self, id_):
        c = self.db.cursor()
        c.execute("UPDATE tweet SET tagged=TRUE WHERE id='" + id_ + "'")
        c.close()
        self.db.commit()

    def setUntagged(self, id_):
        c = self.db.cursor()
        c.execute("UPDATE tweet SET tagged=FALSE WHERE id='" + id_ + "'")
        c.close()
        self.db.commit()

    def setAllUntagged(self):
        c = self.db.cursor()
        c.execute("UPDATE tweet SET tagged=FALSE WHERE tagged=true")
        c.close()
        self.db.commit()

    def close(self):
        self.db.close()

class MongoConnector:
    def __init__(self):
        self.conn = pymongo.Connection()
        db = self.conn['TwitterMining']
        self.tags = db.tagged_tweets

    def insert(self, tagged_tweet):
        self.tags.insert(tagged_tweet)

    def find_os(self, value):
        return self.tags.find({"operating_system_name": value.lower()})

    def find(self, value):
        return self.tags.find({"software_name": value.lower()})

    def find_all(self):
        return self.tags.find()

    def _drop(self):
        self.tags.drop()
        SQLConnector().setAllUntagged()

    def close(self):
        self.conn.close()

if __name__ == '__main__':
    #SQLConnector().deleteUsersNoTweets()
    #MongoConnector()._drop()
    pass

