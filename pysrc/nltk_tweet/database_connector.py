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

    def found(self):
        self.db.query("SELECT id, text, found FROM tweet "
                      + "WHERE tagged=TRUE AND found IS NOT NULL "
                      + "ORDER BY rand() LIMIT 500")
        return self.db.store_result()

    def load_data(self, keyword=None, max_results=15):
        query = ("SELECT id, text, sentiment FROM tweet "
                 + "WHERE tagged=FALSE ")
        if keyword:
            query += "AND keyword='%s' " % keyword
        query += "ORDER BY id DESC LIMIT %d" % max_results

        self.db.query(query)
        return self.db.store_result()

    def get_tweets_nosentiment(self, word=None):
        query = ("SELECT id, text FROM tweet "
                 + "WHERE sentiment IS NULL ")
        if word:
            query += "AND keyword='" + word + "' "
        query += "LIMIT 1000"

        self.db.query(query)
        return self.db.store_result()

    def get_tweet_by_id(self, id):
        return self._get_tweet(condition='tweet_id', expected=id)

    def _get_tweet(self, condition, expected):
        tweet = self._get_tweets(condtion, expected).fetch_row()
        if not len(tweet):
            return None
        return tweet[0]

    def _get_tweets(self, condition, expected):
        self.db.query("SELECT id, text, sentiment FROM tweet "
                      + "WHERE " + condition + "='" + expected +"'")
        return self.db.store_result()

    def get_tweet(self, tweet_id):
        return self._get_tweet(condition='id',expected=tweet_id)

    def update_sentiment(self, id, sentiment, score):
        c = self.db.cursor()
        c.execute("UPDATE tweet SET sentiment='" + sentiment
                  + "', sentiment_score='" + score
                  + "' WHERE id='" + id + "'")
        c.close()
        self.db.commit()

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

    def setFound(self, id_):
        c = self.db.cursor()
        c.execute("UPDATE tweet SET found=TRUE "
                  + "WHERE id='" + id_ + "'")
        c.close()
        self.db.commit()

    def setNotFound(self, id_):
        c = self.db.cursor()
        c.execute("UPDATE tweet SET found=FALSE "
                  + "WHERE id='" + id_ + "'")
        c.close()
        self.db.commit()

    def setAllNotFound(self):
        c = self.db.cursor()
        c.execute("UPDATE tweet SET found=NULL")
        c.close()
        self.db.commit()

    def setAllUntagged(self):
        c = self.db.cursor()
        c.execute("UPDATE tweet SET tagged=FALSE "
                  + "WHERE tagged=true")
        c.close()
        self.db.commit()
        self.setAllNotFound()

    def close(self):
        self.db.close()

class MongoConnector(object):
    def __init__(self, host, db, port=27017):
        super(MongoConnector, self).__init__()
        self._conn = pymongo.Connection(host=host, port=port)
        db = self._conn[db]
        self.tags = db.tagged_tweets
        self.words = db.word_frequency

    def insert(self, **tagged_tweet):
        self.tags.insert(tagged_tweet)

    def update_freqs(self, query, words):
        self.words.update(query, {'$inc':words}, upsert=True)

    def find_os(self, value):
        return self.find(**{"os_name": value.lower()})

    def find_company(self, value):
        return self.find(**{'company_name':value.lower()})

    def find_software(self, value):
        return self.find(**{"software_name": value.lower()})

    def cursor(self, value):
        cursor = self.find_software(value)
        if not cursor.count():
            cursor = self.find_os(value)
        if not cursor.count():
            cursor = self.find_company(value)
        return cursor

    def _group(self, key):
        return self.tags.group(key=[key],
                               condition={key:{'$ne':None}},
                               initial={'count':0},
                               reduce='function(obj,prev) { prev.count += 1;}')

    def top_ten(self):
        from operator import itemgetter
        d = self._group(key='software_name')
        d=flatten(d,self._group('os_name'))

        proper = []
        extras = []
        s = set()
        for map in d:
            proper.append(dict(name=map['os_name'] if 'os_name' in map else map['software_name'], count=map['count']))

        for map in proper[:]:
            if isinstance(map['name'],list):
                extras.append(map)
                proper.remove(map)
            else: s.add(map['name'])

        for map in extras:
            self._aggregate(key='name',
                            map=map,
                            set=s,
                            list=proper)
        return sorted(proper, key=itemgetter('count'), reverse=True)[:10]

    def _aggregate(self, key, map, set, list):
        for name in map[key]:
            if name in set:
                for m in list:
                    if m[key]==name:
                        m['count']=int(m['count'] + map['count'])
            else:
                list.append({key:name,
                             'count':int(map['count'])
                            })

    def find(self, **kwargs):
        return self.tags.find(kwargs)

    def find_all(self):
        cursor = self.find()
        software = cursor.distinct('software_name')
        os = cursor.distinct('os_name')
        company = cursor.distinct('company_name')
        cursor.close()
        return flatten(flatten(software,os),company)

    def drop(self, sql=None):
        self.tags.drop()
        self.words.drop()
        if sql:
            sql.setAllUntagged()

    def close(self):
        self._conn.close()

