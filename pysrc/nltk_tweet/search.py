'''
Created on Feb 21, 2011

@author: Tariq Patel
'''

import sys

from database_connector import MongoConnector

def main():
    mongo = MongoConnector()
    cursor = mongo.find("Adium")
    print cursor.count()
    return 0

if __name__ == '__main__':
    sys.exit(main())
