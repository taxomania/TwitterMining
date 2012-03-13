package uk.ac.manchester.cs.patelt9.twitter.data;

/**
 * Represent Tweets sent on Twitter with its id, text, time of creation and tweeting user stored
 *
 * @author Tariq Patel
 *
 */
public class Tweet {
    private final long tweetId;
    private final String tweet;
    private final String createdAt;
    private final User user;
    private String keyword = null; // Only used by keyword filter

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
    public Tweet(final long id, final String text, final String createdAt, final User user) {
        tweetId = id;
        tweet = text;
        this.createdAt = createdAt;
        this.user = user;
    } // Tweet(long, String, String, User)

    /**
     * Set the keyword used to filter tweets
     *
     * @param keyword
     *            The filter used to find the tweet
     */
    public void setKeyword(final String keyword) {
        this.keyword = keyword;
    } // setKeyword(String)

    /**
     *
     * @return The keyword used to find this tweet or null if none was used
     */
    public String getKeyword() {
        return keyword;
    } // getKeyword()

    /**
     *
     * @return Tweet id
     */
    public long getId() {
        return tweetId;
    } // getId()

    /**
     *
     * @return User id
     */
    public long getUserId() {
        return user.getId();
    } // getUserId()

    /**
     *
     * @return User's screen name
     */
    public String getScreenName() {
        return user.getUsername();
    } // getScreenName()

    /**
     *
     * @return The User object associated with this tweet
     * @see User
     */
    public User getUser() {
        return user;
    } // getUser()

    /**
     *
     * @return The text content of the tweet
     */
    public String getTweet() {
        return tweet;
    } // getTweet()

    /**
     *
     * @return Time of creation
     */
    public String getCreatedAt() {
        return createdAt;
    } // getCreatedAt()

    @Override
    public String toString() {
        return getTweet();
    } // toString()
} // Tweet
