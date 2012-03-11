'''
Created on Feb 21, 2011

@author: Tariq Patel
'''
import sys

from matplotlib import use
use('TkAgg')
from matplotlib.pylab import *
from nltk import flatten

from database_connector import MongoConnector

class ImgCreator(object):
    def __init__(self, args):
        self._mongo = MongoConnector(host=args.host, port=args.mongoport, db=args.db)

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

    def piechart(self, title_, labels_, fracs):
        figure(1, figsize=(6,6))
        ax = axes([0.1,0.1,0.8,0.8])

        pie(fracs, explode=None, labels=labels_, autopct='%1.1f%%', shadow=False)
        title(title_, bbox={'facecolor':'0.8', 'pad':5})

        show()

    def analyse_tool(self, cursor, tool):
        sentiments = cursor.distinct("sentiment")
        print sentiments
        fracs = []
        for sent in sentiments:
            count = 0
            for tag in cursor:
                if sent == tag['sentiment']:
                    count+=1
            fracs.append(count)
            cursor.rewind()
        print fracs
        self.piechart(tool, sentiments, fracs)
        cursor.close()

    def get_cursor(self, word):
        cursor = self._mongo.find(word)
        if cursor.count() == 0:
            cursor = self._mongo.find_os(word)
        return cursor

def main():
    from argument_parser import argument_parser
    args = argument_parser().parse_args('-h localhost -d TwitterMining -m 27017'.split())

    imgc = ImgCreator(args)
    imgc.query(*sys.argv[1:])
    imgc.close()
    return 0

if __name__ == '__main__':
    sys.exit(main())

