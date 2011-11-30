package uk.ac.manchester.cs.patelt9.twitter.data.db.task;

import uk.ac.manchester.cs.patelt9.twitter.data.Tweet;
import uk.ac.manchester.cs.patelt9.twitter.data.db.DatabaseConnector;

/**
 * DatabaseTask allowing database insertion.
 *
 * @author Tariq Patel
 *
 */
public class InsertTask implements DatabaseTask {
    private final Tweet tweet;

    /**
     *
     * @param t
     *            Tweet to insert into database
     */
    public InsertTask(final Tweet t) {
        tweet = t;
    } // InsertTask(Tweet)

    protected Tweet getTweet() {
        return tweet;
    } // getTweet()

    @Override
    public String toString() {
        return tweet.toString();
    } // toString()

    @Override
    public int doTask(final DatabaseConnector db) {
        return db.insertTweet(tweet);
    } // doTask(DatabaseConnector)
} // InsertTask
