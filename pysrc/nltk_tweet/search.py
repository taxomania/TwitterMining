'''
Created on Feb 21, 2011

@author: Tariq Patel
'''
import sys

from database_connector import MongoConnector

def main():
    args = sys.argv[1:]
    if len(args) == 0:
        print "Please provide search terms"
        return 1
    mongo = MongoConnector()
    cursors = []
    for word in args:
        cursor = mongo.find(word)
        print word, cursor.count()
        cursors.append(cursor)
    return 0

if __name__ == '__main__':
    sys.exit(main())
