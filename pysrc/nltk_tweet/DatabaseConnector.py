'''
Created on Dec 18, 2011

@author: Tariq Patel
'''

import MySQLdb as sql
from string import split

class SQLConnector:

    __userpass_retrieved = False
    __user = None
    __pass = None

    @staticmethod
    def _getDetails():
        fin = open("../../sqluserpass.txt")
        strings = split(fin.readline(), ":")
        SQLConnector.__user = strings[0]
        SQLConnector.__pass = strings[1]
        fin.close()
        SQLConnector.__userpass_retrieved = True

    def __init__(self):
        if (not SQLConnector.__userpass_retrieved):
            SQLConnector._getDetails()
        self.db = sql.connect(host='localhost',
                              user=SQLConnector.__user,
                              passwd=SQLConnector.__pass,
                              db='TwitterMining')

    def load_data(self):
        self.db.query("SELECT id, text, sentiment FROM tweet ORDER BY id DESC LIMIT 100")
        return self.db.store_result()

    def isSoftware(self, word):
        return self.__isEntry("SELECT t.type, d.id FROM dictionary d, dict_type t WHERE d.type = t.id AND d.software_name = '" + word + "'")

    def getSoftware(self):
        return self.__getEntry()

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
        return self.__isEntry("SELECT id FROM prog_lang WHERE language = '" + word + "'")

    def getProgLang(self):
        return self.__getEntry()

    def isCompany(self, word):
        return self.__isEntry("SELECT id FROM company WHERE name = '" + word + "'")

    def getCompany(self):
        return self.__getEntry()

    def deleteUsersNoTweets(self):
        self.db.query("DELETE FROM user WHERE NOT EXISTS (SELECT NULL FROM tweet WHERE user.id=tweet.user_id)")
        self.db.commit()
        print "Deleted users with no associated tweets"

    def close(self):
        self.db.close()

if __name__ == '__main__':
    #SQLConnector().deleteUsersNoTweets()
    pass
