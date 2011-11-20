package uk.ac.manchester.cs.patelt9.twitter.data.task.mongo;

import uk.ac.manchester.cs.patelt9.twitter.data.DatabaseConnector;
import uk.ac.manchester.cs.patelt9.twitter.data.MongoConnector;
import uk.ac.manchester.cs.patelt9.twitter.data.task.SentimentTask;

public class SentimentMongoTask extends SentimentTask implements MongoTask {
    private final String sentimentScore;

    public SentimentMongoTask(final long id, final String s) {
        this(id, s, null);
    } // SentimentSQLTask(long, String)

    public SentimentMongoTask(final long id, final String s, final String score) {
        super(id, s);
        sentimentScore = score;
    } // SentimentMongoTask(long, String, String)

    @Override
    public int doMongoTask(final MongoConnector mongo) {
        return mongo.updateSentiment(getId(), getSentiment(), sentimentScore);
    } // doMongoTask(SqlConnector)

    @Override
    public int doTask(final DatabaseConnector db) {
        return db.updateSentiment(getId(), getSentiment(), sentimentScore);
    } // doTask(DatabaseConnector)

    @Override
    public String toString() {
        return super.toString() + ": " + sentimentScore;
    } // toString()
} // SentimentMongoTask
