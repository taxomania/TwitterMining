package uk.ac.manchester.cs.patelt9.twitter.data;

public final class Tweet {
    private final long tweetId;
    private final String tweet;
    private final String createdAt;
    private final User user;
    private String keyword = null; // Only used by keyword filter

    public Tweet(final long id, final String text, final String createdAt, final User user) {
        tweetId = id;
        tweet = text;
        this.createdAt = createdAt;
        this.user = user;
    } // Tweet(long, String, String, User)

    public void setKeyword(final String keyword) {
        this.keyword = keyword;
    } // setKeyword(String)

    public String getKeyword() {
        return keyword;
    } // getKeyword()

    public long getId() {
        return tweetId;
    } // getId()

    public long getUserId() {
        return user.getId();
    } // getUserId()

    public String getScreenName() {
        return user.getUsername();
    } // getScreenName()

    public User getUser() {
        return user;
    } // getUser()

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
