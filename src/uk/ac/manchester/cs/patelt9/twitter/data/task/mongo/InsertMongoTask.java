package uk.ac.manchester.cs.patelt9.twitter.data.task.mongo;

import uk.ac.manchester.cs.patelt9.twitter.data.MongoConnector;
import uk.ac.manchester.cs.patelt9.twitter.data.Tweet;
import uk.ac.manchester.cs.patelt9.twitter.data.task.InsertTask;

public class InsertMongoTask extends InsertTask implements MongoTask {
    public InsertMongoTask(final Tweet t) {
        super(t);
    } // InsertSQLTask(Tweet)

    @Override
    public int doMongoTask(final MongoConnector mongo) {
        return mongo.insertTweet(getTweet());
    } // doMongoTask(MongoConnector)
} // InsertMongoTask