package uk.ac.manchester.cs.patelt9.twitter;

public class StreamingApiFilter extends StreamingApi {
    // URL for Twitter Streaming API filter; filter by software here
    private static final String TWITTER_STREAM_API = "https://stream.twitter.com/1/statuses/filter.json?track=software";

    private static StreamingApiFilter stream = null;

    // Singleton lock as you cannot have more than one connection
    // This may not be needed
    public static StreamingApiFilter getInstance() {
        if (stream == null) {
            stream = new StreamingApiFilter();
        } // if
        return stream;
    } // getInstance()

    private StreamingApiFilter() {
        super(TWITTER_STREAM_API);
    } // StreamingApiFilter()

    // public StreamingApiFilter(final String s){
    // super(TWITTER_STREAM_API + s); // TWITTER_STREAM_API = "...?track="
    // } // StreamingApiFilter(String)

} // StreamingApiFilter
