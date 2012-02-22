'''
Created on Feb 21, 2011

@author: Tariq Patel
'''
import sys

from matplotlib import use
use('TkAgg')
from matplotlib.pylab import *

from database_connector import MongoConnector

def piechart(title_, labels_, fracs):
    figure(1, figsize=(6,6))
    ax = axes([0.1,0.1,0.8,0.8])

    pie(fracs, explode=None, labels=labels_, autopct='%1.1f%%', shadow=False)
    title(title_, bbox={'facecolor':'0.8', 'pad':5})

    show()

def main():
    args = sys.argv[1:]
    mongo = MongoConnector()
    if len(args) == 0:
        # OR SHOW ALL TWEETS
        print mongo.find_all().distinct("software_name")
        return "Please provide search terms"
    for word in args:
        cursor = mongo.find(word)
        total = cursor.count()
        #print word, total
        sentiments = cursor.distinct("sentiment")
        #print sentiments
        fracs = []
        for sent in sentiments:
            count = 0
            for tag in cursor:
                if sent == tag['sentiment']:
                    count+=1
            fracs.append(count)
            cursor.rewind()
        #print fracs
        piechart(word, sentiments, fracs)
    return 0

if __name__ == '__main__':
    sys.exit(main())

