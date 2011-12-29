'''
Created on Dec 18, 2011

@author: Tariq Patel
'''

from string import split

import MySQLdb as sql

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

    def load_data(self, page=0, max_results=100):
        self.db.query("SELECT id, text, sentiment FROM tweet WHERE keyword='latest' ORDER BY id DESC LIMIT "
                      + str(page * max_results) + ', ' + str(max_results))
        return self.db.store_result()

    def isSoftware(self, word):
        return self.__isEntry("SELECT d.id FROM dictionary d, dict_type t "
                              + "WHERE d.type = t.id AND d.software_name = '" + word + "'")

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
        return self.__isEntry("SELECT id FROM prog_lang WHERE language = '" + word + "'")

    def getProgLang(self):
        return self.__getEntry()

    def isOS(self, name):
        return self.__isEntry("SELECT id FROM os WHERE os = '" + name + "'")

    def getOS(self):
        return self.__getEntry()

    def isCompany(self, word):
        return self.__isEntry("SELECT id FROM company WHERE name = '" + word + "'")

    def getCompany(self):
        return self.__getEntry()

    def deleteUsersNoTweets(self):
        self.db.query("DELETE FROM user WHERE NOT EXISTS (SELECT NULL FROM tweet "
                      + "WHERE user.id=tweet.user_id)")
        self.db.commit()
        print "Deleted users with no associated tweets"

    def close(self):
        self.db.close()


if __name__ == '__main__':
    #SQLConnector().deleteUsersNoTweets()
    pass
