package uk.ac.manchester.cs.patelt9.twitter.stream;

import java.net.UnknownHostException;
import java.sql.SQLException;

import com.mongodb.MongoException;

public class StreamingApiSample extends StreamingApi {
    // URL for Twitter Streaming API sample; 1% of all tweets
    private static final String TWITTER_STREAM_API = "https://stream.twitter.com/1/statuses/sample.json";
    private static final int COUNTER_INTERVAL = 500;

    private static StreamingApiSample stream = null;

    // Singleton lock as you cannot have more than one connection
    public static StreamingApiSample getInstance() throws MongoException, UnknownHostException,
            SQLException {
        if (stream == null) {
            stream = new StreamingApiSample();
        } // if
        return stream;
    } // getInstance()

    private StreamingApiSample() throws MongoException, UnknownHostException, SQLException {
        super(TWITTER_STREAM_API, COUNTER_INTERVAL);
    } // StreamingApiSample()
} // StreamingApiSample
