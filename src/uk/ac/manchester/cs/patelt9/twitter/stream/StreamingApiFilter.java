package uk.ac.manchester.cs.patelt9.twitter.stream;

import java.sql.SQLException;

public class StreamingApiFilter extends StreamingApi {
    // URL for Twitter Streaming API filter; filter by software here
    private static final String TWITTER_STREAM_API = "https://stream.twitter.com/1/statuses/filter.json";
    private static final String DEFAULT_QUERY = "?track=";
    private static final String DEFAULT_KEYWORD = "software";
    private static final int COUNTER_INTERVAL = 10;

    private static StreamingApiFilter stream = null;

    protected String keyword = "";

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

    // Will use if no longer require singleton lock
    @SuppressWarnings("unused")
    private StreamingApiFilter() throws SQLException {
        this(DEFAULT_KEYWORD);
    } // StreamingApiFilter()

    private StreamingApiFilter(final String filter) throws SQLException {
        this(TWITTER_STREAM_API + DEFAULT_QUERY + filter, COUNTER_INTERVAL);
        keyword = filter;
    } // StreamingApiFilter(String)

    // For subclasses
    protected StreamingApiFilter(final int interval) throws SQLException {
        this(TWITTER_STREAM_API, interval);
    } // StreamingApiFilter(int)

    private StreamingApiFilter(final String url, final int interval) throws SQLException {
        super(url, interval);
    } // StreamingApiFilter(String, int)

    @Override
    protected int addToDb(final Long tweetId, final String screenName, final String tweet,
            final String createdAt, final Long userId) {
        return getSqlConnector()
                .insertTweet(tweetId, screenName, tweet, createdAt, userId, keyword);
    } // addToDb(Long, String, String, String, Long)

} // StreamingApiFilter
