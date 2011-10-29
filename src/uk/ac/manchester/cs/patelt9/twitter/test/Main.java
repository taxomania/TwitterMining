package uk.ac.manchester.cs.patelt9.twitter.test;

import uk.ac.manchester.cs.patelt9.twitter.SqlConnector;
import uk.ac.manchester.cs.patelt9.twitter.StreamingApi;
import uk.ac.manchester.cs.patelt9.twitter.StreamingApiFilter;

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
            final StreamingApi stream = StreamingApiFilter.getInstance();
            stream.streamTweets();
            stream.close();
        } // else
    } // main(String[])
} // Main