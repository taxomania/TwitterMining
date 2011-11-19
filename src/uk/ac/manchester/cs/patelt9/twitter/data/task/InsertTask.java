package uk.ac.manchester.cs.patelt9.twitter.data.task;

import uk.ac.manchester.cs.patelt9.twitter.data.Tweet;

public abstract class InsertTask implements DatabaseTask {
    private final Tweet tweet;

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
} // InsertTask
