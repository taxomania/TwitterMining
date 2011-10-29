package uk.ac.manchester.cs.patelt9.twitter;

import uk.ac.manchester.cs.patelt9.twitter.data.SqlConnector;
import uk.ac.manchester.cs.patelt9.twitter.stream.StreamingApi;
import uk.ac.manchester.cs.patelt9.twitter.stream.StreamingApiSample;

public class Main {
    public static void main(final String[] args) {
        if (args.length != 0) {
            if (args[0].equals("delete")) {
                final SqlConnector sql = SqlConnector.getInstance();
                System.out.println(sql.deleteAll());
                sql.close();
                return;
            } // if
        } else {
            final StreamingApi stream = StreamingApiSample.getInstance();
            stream.streamTweets();
            stream.close();
        } // else
    } // main(String[])
} // Main