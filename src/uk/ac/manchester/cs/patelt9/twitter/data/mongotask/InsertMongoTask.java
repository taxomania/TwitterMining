package uk.ac.manchester.cs.patelt9.twitter.data.mongotask;

import uk.ac.manchester.cs.patelt9.twitter.data.MongoConnector;
import uk.ac.manchester.cs.patelt9.twitter.data.Tweet;

public class InsertMongoTask implements MongoTask {
    private final Tweet tweet;

    public InsertMongoTask(final Tweet t) {
        tweet = t;
    } // InsertSQLTask(Tweet)

    @Override
    public int doMongoTask(final MongoConnector mongo) {
        return mongo.insertTweet(tweet);
    } // doMongoTask(MongoConnector)

    @Override
    public String toString() {
        return tweet.toString();
    } // toString()
} // InsertMongoTask