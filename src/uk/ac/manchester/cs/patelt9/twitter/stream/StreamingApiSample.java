package uk.ac.manchester.cs.patelt9.twitter.stream;

import java.sql.SQLException;

import uk.ac.manchester.cs.patelt9.twitter.data.SqlConnector;

public class StreamingApiSample extends StreamingApi {
    // URL for Twitter Streaming API sample; 1% of all tweets
    private static final String TWITTER_STREAM_API = "https://stream.twitter.com/1/statuses/sample.json";
    private static final int COUNTER_INTERVAL = 500;

    private static StreamingApiSample stream = null;

    // Singleton lock as you cannot have more than one connection
    public static StreamingApiSample getInstance() throws SQLException {
        if (stream == null) {
            stream = new StreamingApiSample();
        } // if
        return stream;
    } // getInstance()

    public static StreamingApiSample getInstance(final SqlConnector sql) {
        if (stream == null) {
            stream = new StreamingApiSample(sql);
        } // if
        return stream;
    } // getInstance(SqlConnector)

    private StreamingApiSample(final SqlConnector sql) {
        super(sql, TWITTER_STREAM_API, COUNTER_INTERVAL);
    } // StreamingApiSample(SqlConnector)

    private StreamingApiSample() throws SQLException {
        super(TWITTER_STREAM_API, COUNTER_INTERVAL);
    } // StreamingApiSample()
} // StreamingApiSample
