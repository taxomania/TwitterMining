package uk.ac.manchester.cs.patelt9.twitter.data.db.task;

import uk.ac.manchester.cs.patelt9.twitter.data.db.TweetDatabaseConnector;

/**
 * DatabaseTask which updates the sentiment of a tweet
 *
 * @author Tariq Patel
 *
 */
public class SentimentScoreTask extends SentimentTask {
    private final String sentimentScore;

    /**
     *
     * @param id
     *            Tweet id
     * @param s
     *            Sentiment result - positive/negative/neutral
     */
    public SentimentScoreTask(final long id, final String s) {
        this(id, s, null);
    } // SentimentScoreTask(long, String)

    /**
     *
     * @param id
     *            Tweet id
     * @param s
     *            Sentiment result - positive/negative/neutral
     * @param score
     *            Certainty of given sentiment result in String form
     */
    public SentimentScoreTask(final long id, final String s, final String score) {
        super(id, s);
        sentimentScore = score;
    } // SentimentScoreTask(long, String, String)

    @Override
    public int doTask(final TweetDatabaseConnector db) {
        return db.updateSentiment(getId(), getSentiment(), sentimentScore);
    } // doTask(TweetDatabaseConnector)

    @Override
    public String toString() {
        return super.toString() + COLON + sentimentScore;
    } // toString()
} // SentimentScoreTask
