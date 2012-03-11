'''
Created on Feb 21, 2011

@author: Tariq Patel
'''
import sys

from nltk import flatten

from database_connector import MongoConnector

class ImgCreator(object):
    def __init__(self, args):
        self._mongo = MongoConnector(host=args.H, port=args.mongoport, db=args.db)

    def web_query(self, word):
        word = word.lower()
        return self.analyse(self.get_cursor(word), word)

    def query(self, *args):
        if len(args) == 0:
            self._show_all()
        else:
            for word in args:
                self.analyse_tool(self.get_cursor(word), word)

    def _show_all(self):
        cursor = self._mongo.find_all()
        software = cursor.distinct("software_name")
        os = cursor.distinct("os_name")
        tools = flatten(software,os)
        print tools
        cursor.close()
        for word in tools:
            self.analyse_tool(self.get_cursor(word), word)

    def close(self):
        self._mongo.close()

    def analyse(self, cursor, tool):
        sentiments = cursor.distinct('sentiment')
        data = []
        print cursor.count(),sentiments
        for sentiment in sentiments:
            count = 0
            for tag in cursor:
                if sentiment == tag['sentiment']:
                    count += 1
            data.append([str(sentiment),count])
            cursor.rewind()
        cursor.close()
        return data

    def get_cursor(self, word):
        cursor = self._mongo.find(word)
        if cursor.count() == 0:
            cursor = self._mongo.find_os(word)
        return cursor

def main():
    from argument_parser import argument_parser
    args = argument_parser().parse_args('-d TwitterMining -m 27017'.split())
    args.H = 'localhost'

    imgc = ImgCreator(args)
    print imgc.web_query('android')
    #imgc.query(*sys.argv[1:])
    imgc.close()
    return 0

if __name__ == '__main__':
    sys.exit(main())

