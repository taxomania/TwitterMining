package uk.ac.manchester.cs.patelt9.twitter.stream;

import java.sql.SQLException;

public class StreamingApiFilter extends StreamingApi {
    // URL for Twitter Streaming API filter; filter by software here
    private static final String TWITTER_STREAM_API = "https://stream.twitter.com/1/statuses/filter.json?track=";
    private static final String DEFAULT_KEYWORD = "software";
    private static final int COUNTER_INTERVAL = 10;

    private static StreamingApiFilter stream = null;

    private final String keyword;

    // Singleton lock as you cannot have more than one connection
    // This may not be needed
    public static StreamingApiFilter getInstance() throws SQLException {
        return getInstance(DEFAULT_KEYWORD);
    } // getInstance()

    public static StreamingApiFilter getInstance(final String filter) throws SQLException {
        if (stream == null) {
            stream = new StreamingApiFilter(filter);
        } // if
        return stream;
    } // getInstance(String)

    private StreamingApiFilter() throws SQLException {
        this(DEFAULT_KEYWORD);
    } // StreamingApiFilter()

    private StreamingApiFilter(final String filter) throws SQLException {
        super(TWITTER_STREAM_API + filter, COUNTER_INTERVAL);
        keyword = filter;
    } // StreamingApiFilter(String)

    @Override
    protected int addToDb(final Long tweetId, final String screenName, final String tweet,
            final String createdAt, final Long userId) {
        return getSqlConnector()
                .insertTweet(tweetId, screenName, tweet, createdAt, userId, keyword);
    } // addToDb(Long, String, String, String, Long)

} // StreamingApiFilter
