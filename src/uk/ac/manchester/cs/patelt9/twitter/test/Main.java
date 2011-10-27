package uk.ac.manchester.cs.patelt9.twitter.test;

import uk.ac.manchester.cs.patelt9.twitter.SqlConnector;
import uk.ac.manchester.cs.patelt9.twitter.StreamingApi;

public class Main {
    public static void main(final String[] args) {
        final StreamingApi stream = StreamingApi.getInstance();
        stream.streamTweets();
        stream.disconnect();

        if (args.length != 0) {
            if (args[0].equals("delete")) {
                final SqlConnector sql = SqlConnector.getInstance();
                System.out.println(sql.deleteAll());
                sql.close();
                return;
            } // if
        } // if
    } // main(String[])
} // Main
