'''
@author: Tariq Patel
'''

from database_connector import *

class Found(object):
    def __init__(self, sql=None, mongo=None, **kwargs):
        super(Found, self).__init__()
        if not sql:
            self._sql = SQLConnector(host=kwargs['host'],
                                     port=kwargs['port'],
                                     user=kwargs['user'],
                                     passwd=kwargs['password'],
                                     db=kwargs['db'])
        else:
            self._sql = sql
        if not mongo:
            self._mongo = MongoConnector(host=kwargs['H'], port=kwargs['mongoport'], db=kwargs['db'])
        else:
            self._mongo = mongo

    def close(self):
        self._sql.close()
        self._mongo.close()

    def find(self):
        res = self._sql.found()
        rows = res.num_rows()
        if not rows:
            print "No tweets found"
            return
        for _i_ in range(rows):
            for tweet in res.fetch_row():
                id_ = str(tweet[0])
                text = tweet[1]
                found = True if tweet[2] else False


                if found:
                    a = self._mongo.find(**{'tweet_db_id':id_})
                    for b in a:
                        print b
                else:
                    print text
                    print found
                
                print


def main(args):
    args = vars(args)
    f = Found(**args)
    f.find()
    f.close()
    return 0

if __name__ == "__main__":
    import argument_parser
    import sys
    sys.exit(main(argument_parser.main()))

