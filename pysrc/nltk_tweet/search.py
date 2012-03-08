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

def piechart(title_, labels_, fracs):
    figure(1, figsize=(6,6))
    ax = axes([0.1,0.1,0.8,0.8])

    pie(fracs, explode=None, labels=labels_, autopct='%1.1f%%', shadow=False)
    title(title_, bbox={'facecolor':'0.8', 'pad':5})

    show()

def analyse_tool(cursor, tool):
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
    piechart(tool, sentiments, fracs)
    cursor.close()

def get_cursor(word):
    cursor = mongo.find(word)
    if cursor.count() == 0:
        cursor = mongo.find_os(word)
    return cursor

def main():
    args = sys.argv[1:]
    global mongo
    mongo = MongoConnector(host="localhost", db="TwitterMining", port=27017)
    if len(args) == 0:
        # OR SHOW ALL TWEETS
        cursor = mongo.find_all()
        software = cursor.distinct("software_name")
        os = cursor.distinct("os_name")
        tools = flatten(software,os)
        print tools
        cursor.close()
        for word in tools:
            analyse_tool(get_cursor(word), word)
        #return "Please provide search terms"
    else:
        for word in args:
            analyse_tool(get_cursor(word), word)
    mongo.close()
    return 0

if __name__ == '__main__':
    sys.exit(main())

