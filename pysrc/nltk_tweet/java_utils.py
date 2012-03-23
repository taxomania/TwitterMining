import os.path
import subprocess

with open('classpath.txt', 'r') as f:
    cp = f.readline().strip()
 
java_cp = 'java -cp ' + cp + ' uk.ac.manchester.cs.patelt9.twitter.'
 
def search_twitter(query):
    tweets_ = subprocess.check_output(java_cp+'SearchAPI '+ query, shell=True).strip().decode('utf-8', 'ignore').split('\n')
    tweets = []
    for tweet in tweets_:
        tweet_ = tweet.split('\t')
        if len(tweet_) > 1:
            tweets.append(tweet_)
    return tweets

