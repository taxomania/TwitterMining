package uk.ac.manchester.cs.patelt9.twitter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLException;

import uk.ac.manchester.cs.patelt9.twitter.stream.StreamingApi;
import uk.ac.manchester.cs.patelt9.twitter.stream.StreamingApiFilterPost;
import uk.ac.manchester.cs.patelt9.twitter.stream.StreamingApiSample;

public class Stream {
    public static StreamingApi getStream(final String[] args) throws SQLException {
        if (args != null && args.length != 0) {
            if (args[0].equals("sample")) {
                return StreamingApiSample.getInstance();
            } else {
                return StreamingApiFilterPost.getInstance(args);
            } // else
        } else {
            return StreamingApiFilterPost.getInstance();
        } // else
    } // getStream(String[])

   public static void main(final String[] args) {
        final StreamingApi stream;
        try {
            stream = getStream(args);
            try {
                stream.connect();
                stream.streamTweets();
            } catch (final MalformedURLException e) {
                System.err.println("Error parsing URL");
                System.exit(1);
            } catch (final IOException e) {
                System.err.println("Could not connect to server");
                System.exit(1);
            } // catch
        } catch (final SQLException e) {
            System.err.println("Could not connect to database");
            System.exit(1);
        } // catch
    } // main(String[])
} // Stream