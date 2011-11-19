package uk.ac.manchester.cs.patelt9.twitter.data.mongotask;

import uk.ac.manchester.cs.patelt9.twitter.data.MongoConnector;

public class SentimentMongoTask implements MongoTask {
    private final long id;
    private final String sentiment;
    private final String sentimentScore;

    public SentimentMongoTask(final long id, final String s) {
        this(id, s, null);
    } // SentimentSQLTask(long, String)

    public SentimentMongoTask(final long id, final String s, final String score) {
        this.id = id;
        sentiment = s;
        sentimentScore = score;
    } // SentimentSQLTask(long, String, String)

    @Override
    public int doMongoTask(final MongoConnector mongo) {
        return mongo.updateSentiment(id, sentiment, sentimentScore);
    } // doSqlTask(SqlConnector)

    @Override
    public String toString() {
        return Long.toString(id) + ": " + sentiment + ": " + sentimentScore;
    } // toString()
} // SentimentMongoTask
