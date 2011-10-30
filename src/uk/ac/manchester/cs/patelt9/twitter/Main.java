package uk.ac.manchester.cs.patelt9.twitter;

import uk.ac.manchester.cs.patelt9.twitter.data.SentimentAnalysis;
import uk.ac.manchester.cs.patelt9.twitter.data.SqlConnector;
import uk.ac.manchester.cs.patelt9.twitter.stream.StreamingApi;
import uk.ac.manchester.cs.patelt9.twitter.stream.StreamingApiFilter;
import uk.ac.manchester.cs.patelt9.twitter.stream.StreamingApiSample;

public class Main {
    public static void main(final String[] args) {
        if (args.length != 0) {
            if (args[0].equals("delete")) {
                final SqlConnector sql = SqlConnector.getInstance();
                System.out.println(sql.deleteAll());
                sql.close();
                return;
            } else if (args[0].equals("analyse")) {
                final SentimentAnalysis sa = SentimentAnalysis.getInstance();
                sa.loadDataSet();
                sa.analyseSentiment();
                sa.close();
            } else {
                final StreamingApi stream;
                if (args[0].equals("sample")) {
                    stream = StreamingApiSample.getInstance();
                } else {
                    stream = StreamingApiFilter.getInstance(args[0]);
                } // else
                stream.streamTweets();
                stream.close();
            } // else
        } else {
            final StreamingApi stream = StreamingApiFilter.getInstance();
            stream.streamTweets();
            stream.close();
        } // else
    } // main(String[])
} // Main