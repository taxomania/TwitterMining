package uk.ac.manchester.cs.patelt9.twitter;

public class Tweet {
    private final Long tweetId;
    private final Long userId;
    private final String screenName;
    private final String tweet;
    private final String createdAt;

    public Tweet(final Long id, final Long userId, final String username, final String tweet,
            final String createdAt) {
        tweetId = id;
        this.userId = userId;
        screenName = username;
        this.tweet = tweet;
        this.createdAt = createdAt;
    } // Tweet(Long, Long, String, String, String)

    public Long getId() {
        return tweetId;
    } // getId()

    public Long getUserId() {
        return userId;
    } // getUserId()

    public String getScreenName() {
        return screenName;
    } // getScreenName()

    public String getTweet() {
        return tweet;
    } // getTweet()

    public String getCreatedAt() {
        return createdAt;
    } // getCreatedAt()

    @Override
    public String toString() {
        return getTweet();
    } // toString()
} // Tweet
