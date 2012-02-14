package uk.ac.manchester.cs.patelt9.twitter.data.db.task;

import uk.ac.manchester.cs.patelt9.twitter.data.db.TweetDatabaseConnector;

/**
 * DatabaseTask which updates the sentiment of a tweet
 *
 * @author Tariq Patel
 *
 */
public class SentimentTask implements DatabaseTask {
    protected static final String COLON = ": ";
    private final long id;
    private final String sentiment;

    /**
     *
     * @param id
     *            Tweet id
     * @param s
     *            Sentiment result - positive/negative/neutral
     */
    public SentimentTask(final long id, final String s) {
        this.id = id;
        sentiment = s;
    } // SentimentTask(long, String)

    protected long getId() {
        return id;
    } // getId()

    protected String getSentiment() {
        return sentiment;
    } // getSentiment()

    @Override
    public int doTask(final TweetDatabaseConnector db) {
        return db.updateSentiment(id, sentiment);
    } // doTask(TweetDatabaseConnector)

    @Override
    public String toString() {
        return Long.toString(id) + COLON + sentiment;
    } // toString()
} // SentimentTask
