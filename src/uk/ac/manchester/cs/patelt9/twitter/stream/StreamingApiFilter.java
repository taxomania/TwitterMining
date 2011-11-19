package uk.ac.manchester.cs.patelt9.twitter.stream;

import java.net.UnknownHostException;

import com.mongodb.MongoException;

public class StreamingApiFilter extends StreamingApi {
    // URL for Twitter Streaming API filter; filter by software here
    private static final String TWITTER_STREAM_API = "https://stream.twitter.com/1/statuses/filter.json";
    private static final String DEFAULT_QUERY = "?track=";
    private static final String DEFAULT_KEYWORD = "software";
    private static final int COUNTER_INTERVAL = 10;

    private static StreamingApiFilter stream = null;

    protected String keyword = "";

    // Singleton lock as you cannot have more than one connection
    public static StreamingApiFilter getInstance() throws UnknownHostException, MongoException {
        return getInstance(DEFAULT_KEYWORD);
    } // getInstance()

    public static StreamingApiFilter getInstance(final String filter) throws MongoException,
            UnknownHostException {
        if (stream == null) {
            stream = new StreamingApiFilter(filter);
        } // if
        return stream;
    } // getInstance(String)

    private StreamingApiFilter(final String filter) throws MongoException, UnknownHostException {
        this(TWITTER_STREAM_API + DEFAULT_QUERY + filter, COUNTER_INTERVAL);
    } // StreamingApiFilter(String)

    // For subclasses
    protected StreamingApiFilter(final int interval) throws MongoException, UnknownHostException {
        this(TWITTER_STREAM_API, interval);
    } // StreamingApiFilter(int)

    private StreamingApiFilter(final String url, final int interval) throws MongoException,
            UnknownHostException {
        super(url, interval);
    } // StreamingApiFilter(String, int)
} // StreamingApiFilter
