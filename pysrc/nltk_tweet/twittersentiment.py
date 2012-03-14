'''
Created on Mar 13, 2012
@author: Tariq Patel
'''

from httplib2 import Http
from json import loads

def parse_list(sql, keyword):
    print "Retrieving tweets"
    res = sql.get_tweets_keyword_nosentiment(word=keyword)
    rows = res.num_rows()
    if not rows:
        return None
    data = []
    for i in range(rows):
        for tweet in res.fetch_row():
            data.append(dict(id=tweet[0], text=tweet[1]))
    return data

def bulk_analysis(sql, keyword):
    h = Http()
    data = dict(data=parse_list(sql, keyword))
    print "Analysing sentiment"
    resp_, content = h.request("http://twittersentiment.appspot.com/api/bulkClassifyJson", "POST", str(data))
    print resp_, content
    #resp = loads(resp_)
    #if resp['content-length']:
    if content:    
        content = loads(content.decode('utf-8', 'ignore'))
        update_db(sql, content)

def update_db(sql, content):
    content = content['data']
    for tweet in content:
        score= tweet['polarity']
        if score == 4:
            sentiment = 'positive'
        elif score == 0:
            sentiment = 'negative'
        else:
            sentiment = 'neutral'
        sql.update_sentiment(id=str(tweet['id']),
                             sentiment=sentiment,
                             score=str(score))

