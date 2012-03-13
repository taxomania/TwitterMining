'''
Created on Dec 18, 2011

@author: Tariq Patel
'''

from string import split

from nltk import flatten

import MySQLdb as sql
import pymongo

class SQLConnector(object):
    def __init__(self, host, user, passwd, db, port=3306):
        super(SQLConnector, self).__init__()
        self.db = sql.connect(host=host,
                              port=port,
                              user=user,
                              passwd=passwd,
                              db=db)

    def load_data(self, page=0, max_results=100):
        self.db.query("SELECT id, text, sentiment FROM tweet "
                      + "WHERE keyword='latest' "
                      + "AND tagged=FALSE ORDER BY id DESC "
                      + "LIMIT "
                      + str(page * max_results) + ', '
                      + str(max_results))
        return self.db.store_result()

    def get_tweet_by_id(self, id):
        return self._get_tweet(condition='tweet_id', id=id)

    def _get_tweet(self, condition, id):
        self.db.query("SELECT id, text, sentiment FROM tweet "
                      + "WHERE " + condition + "='" + id +"'")
        tweet_tuple = self.db.store_result().fetch_row()
        if not len(tweet_tuple):
            return None
        return tweet_tuple[0]

    def get_tweet(self, tweet_id):
        return self._get_tweet(condition='id',id=tweet_id)

    def isSoftware(self, word):
        return self._isEntry("SELECT d.id, d.software_name "
                             + "FROM dictionary d, "
                             + "dict_type t "
                             + "WHERE d.type = t.id "
                             + "AND d.software_name = "
                             + "'" + word + "'")

    def getSoftware(self):
        return self._getEntry()

    def insertSoftware(self, name):
        c = self.db.cursor()
        c.execute("INSERT INTO dictionary(software_name) "
                  + "VALUES('"+name+"')")
        id_ = self.db.insert_id()
        c.close()
        self.db.commit()
        return id_

    def _getEntry(self):
        return self.result[0]

    def _isEntry(self, query):
        self.db.query(query)
        self.result = self.db.store_result().fetch_row()
        size = len(self.result)
        if (size == 0):
            return False
        else:
            return True

    def isProgLang(self, word):
        return self._isEntry("SELECT id, language "
                             + "FROM prog_lang "
                             + "WHERE language = "
                             + "'" + word + "'")

    def getProgLang(self):
        return self._getEntry()

    def isOS(self, name):
        return self._isEntry("SELECT id, os FROM os "
                             + "WHERE os = '" + name + "'")

    def getOS(self):
        return self._getEntry()

    def isCompany(self, word):
        return self._isEntry("SELECT id, name FROM company "
                             + "WHERE name = '" + word + "'")

    def getCompany(self):
        return self._getEntry()

    def deleteUsersNoTweets(self):
        self.db.query("DELETE FROM user WHERE NOT EXISTS "
                      + "(SELECT NULL FROM tweet "
                      + "WHERE user.id=tweet.user_id)")
        self.db.commit()
        print "Deleted users with no associated tweets"

    def setTagged(self, id_):
        c = self.db.cursor()
        c.execute("UPDATE tweet SET tagged=TRUE "
                  + "WHERE id='" + id_ + "'")
        c.close()
        self.db.commit()

    def setUntagged(self, id_):
        c = self.db.cursor()
        c.execute("UPDATE tweet SET tagged=FALSE "
                  + "WHERE id='" + id_ + "'")
        c.close()
        self.db.commit()

    def setAllUntagged(self):
        c = self.db.cursor()
        c.execute("UPDATE tweet SET tagged=FALSE "
                  + "WHERE tagged=true")
        c.close()
        self.db.commit()

    def close(self):
        self.db.close()

class MongoConnector(object):
    def __init__(self, host, db, port=27017):
        super(MongoConnector, self).__init__()
        self._conn = pymongo.Connection(host=host, port=port)
        db = self._conn[db]
        self.tags = db.tagged_tweets

    def insert(self, **tagged_tweet):
        self.tags.insert(tagged_tweet)

    def find_os(self, value):
        return self.tags.find({"os_name": value.lower()})

    def find(self, value):
        return self.tags.find({"software_name": value.lower()})

    def find_all(self):
        return self.tags.find()

    def find_all_software_os(self):
        cursor = self.find_all()
        software = cursor.distinct('software_name')
        os = cursor.distinct('os_name')
        cursor.close()
        return flatten(software,os)

    def _drop(self):
        self.tags.drop()
        SQLConnector().setAllUntagged()

    def close(self):
        self._conn.close()

if __name__ == '__main__':
    #SQLConnector().deleteUsersNoTweets()
    #MongoConnector()._drop()
    pass

