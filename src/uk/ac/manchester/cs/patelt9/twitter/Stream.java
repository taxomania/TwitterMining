package uk.ac.manchester.cs.patelt9.twitter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.sql.SQLException;

import uk.ac.manchester.cs.patelt9.twitter.stream.StreamingApi;
import uk.ac.manchester.cs.patelt9.twitter.stream.StreamingApiFilter;
import uk.ac.manchester.cs.patelt9.twitter.stream.StreamingApiSample;

import com.mongodb.MongoException;

public class Stream {
    public static StreamingApi getStream(final String[] args) throws MongoException,
            UnknownHostException, SQLException {
        if (args != null && args.length != 0) {
            if (args[0].equals("sample")) { return StreamingApiSample.getInstance(); }
        } // if
        return StreamingApiFilter.getInstance();
    }// getStream(String[])

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
        } catch (final MongoException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (final UnknownHostException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (final SQLException e) {
            e.printStackTrace();
            System.exit(1);
        } // catch
    } // main(String[])
} // Stream
