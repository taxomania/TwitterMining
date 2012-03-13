package uk.ac.manchester.cs.patelt9.twitter.data;

/**
 * Represent Tweets sent on Twitter with its id, text, time of creation and tweeting user stored
 *
 * @author Tariq Patel
 *
 */
public final class TweetVerbose extends Tweet {
    /**
     *
     * @param id
     *            Tweet id as provided by Twitter
     * @param text
     *            The text content of the tweet
     * @param createdAt
     *            Time of creation
     * @param user
     *            User who sent the tweet
     * @see User
     */
    public TweetVerbose(final long id, final String text, final String createdAt, final User user) {
        super(id, text, createdAt, user);
    } // TweetVerbose(long, String, String, User)

    @Override
    public String toString() {
        return getId() + "\t" + getTweet();
    } // toString()
} // TweetVerbose
