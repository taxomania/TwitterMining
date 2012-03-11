'''
Created on Feb 21, 2011

@author: Tariq Patel
'''
import sys

from nltk import flatten

from database_connector import MongoConnector

class ImgCreator(object):
    def __init__(self, mongo=None, **kwargs):
        if not mongo:
            self._mongo = MongoConnector(host=kwargs['H'], port=kwargs['mongoport'], db=kwargs['db'])
        else:
            self._mongo = mongo

    def web_query(self, word):
        word = word.lower()
        return self.analyse(self.get_cursor(word), word)

    def query(self, *args):
        if len(args) == 0:
            self._show_all()
        else:
            for word in args:
                self.analyse(self.get_cursor(word), word)

    def _show_all(self):
        tools = self._mongo.find_all_software_os()
        print tools
        for word in tools:
            self.analyse(self.get_cursor(word), word)

    def close(self):
        self._mongo.close()

    def analyse(self, cursor, tool):
        sentiments = cursor.distinct('sentiment')
        data = []
        colours = []
        print cursor.count(),sentiments
        for sentiment in sentiments:
            count = 0
            for tag in cursor:
                if sentiment == tag['sentiment']:
                    count += 1
            data.append([str(sentiment),count])
            cursor.rewind()
            if sentiment == 'neutral':
                colours.append('blue')
            elif sentiment == 'positive':
                colours.append('green')
            elif sentiment == 'negative':
                colours.append('red')
        cursor.close()
        return data, colours

    def get_cursor(self, word):
        cursor = self._mongo.find(word)
        if cursor.count() == 0:
            cursor = self._mongo.find_os(word)
        return cursor

def main():
    from argument_parser import argument_parser
    args = argument_parser().parse_args('-d TwitterMining -m 27017'.split())
    args.H = 'localhost'

    args = vars(args)
    imgc = ImgCreator(**args)
    #print imgc.web_query('android')
    imgc.query(*sys.argv[1:])
    imgc.close()
    return 0

if __name__ == '__main__':
    sys.exit(main())

