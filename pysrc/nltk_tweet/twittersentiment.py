'''
Created on Mar 13, 2012
@author: Tariq Patel
'''

from httplib2 import Http
from json import loads

def parse_list(sql, keyword):
    print "Retrieving tweets"
    res = sql.get_tweets_nosentiment(word=keyword)
    rows = res.num_rows()
    if not rows:
        return None
    data = []
    for i in range(rows):
        for tweet in res.fetch_row():
            data.append(dict(id=str(tweet[0]), text=tweet[1]))
    return data

def bulk_analysis(sql, keyword=None, iterations=10):
    count = 0
    for i in xrange(iterations):
        h = Http()
        tweets = parse_list(sql, keyword)
        if not tweets:
            return "No tweets to analyse"
        data = dict(data=tweets)
        print "Analysing sentiment"
        resp, content = h.request("http://twittersentiment.appspot.com/api/bulkClassifyJson", "POST", str(data))
        if content:
            content = loads(content.decode('utf-8', 'ignore'))
            _update_db(sql, content)
            count += len(tweets)
        else:
            return "Sentiment analysis failed"
    return "%d tweets analysed for sentiment" % count

def _update_db(sql, content):
    content = content['data']
    for tweet in content:
        score= tweet['polarity']
        if score == 4:
            sentiment = 'positive'
        elif score == 0:
            sentiment = 'negative'
        else:
            sentiment = 'neutral'
        sql.update_sentiment(id=tweet['id'],
                             sentiment=sentiment,
                             score=str(score))

