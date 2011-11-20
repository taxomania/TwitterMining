package uk.ac.manchester.cs.patelt9.twitter.data.task;

import uk.ac.manchester.cs.patelt9.twitter.data.DatabaseConnector;

public class SentimentScoreTask extends SentimentTask {
    private final String sentimentScore;

    public SentimentScoreTask(final long id, final String s) {
        this(id, s, null);
    } // SentimentScoreTask(long, String)

    public SentimentScoreTask(final long id, final String s, final String score) {
        super(id, s);
        sentimentScore = score;
    } // SentimentScoreTask(long, String, String)

    @Override
    public int doTask(final DatabaseConnector db) {
        return db.updateSentiment(getId(), getSentiment(), sentimentScore);
    } // doTask(DatabaseConnector)

    @Override
    public String toString() {
        return super.toString() + ": " + sentimentScore;
    } // toString()
} // SentimentScoreTask
