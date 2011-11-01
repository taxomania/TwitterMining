package uk.ac.manchester.cs.patelt9.twitter;

import java.io.IOException;
import java.sql.SQLException;

import uk.ac.manchester.cs.patelt9.twitter.data.SentimentAnalysis;
import uk.ac.manchester.cs.patelt9.twitter.data.SqlConnector;
import uk.ac.manchester.cs.patelt9.twitter.stream.StreamingApi;
import uk.ac.manchester.cs.patelt9.twitter.stream.StreamingApiFilter;
import uk.ac.manchester.cs.patelt9.twitter.stream.StreamingApiSample;

public class Main {
    public static void main(final String[] args) {
        if (args.length != 0) {
            if (args[0].equals("delete")) {
                try {
                    final SqlConnector sql = SqlConnector.getInstance();
                    System.out.println(sql.deleteAll());
                    sql.close();
                } catch (final SQLException e) {
                    System.err.println("Failed to delete tables");
                } // catch
                return;
            } else if (args[0].equals("analyse")) {
                try {
                    final SentimentAnalysis sa = SentimentAnalysis.getInstance();
                    sa.loadDataSet();
                    sa.analyseSentiment();
                    sa.close();
                } catch (final SQLException e) {
                    System.err.println("Could not connect to database");
                } catch (final IOException e) {
                    System.err.println("Could not find API key");
                }
            } else {
                try {
                    final StreamingApi stream;
                    if (args[0].equals("sample")) {
                        stream = StreamingApiSample.getInstance();
                    } else {
                        stream = StreamingApiFilter.getInstance(args[0]);
                    } // else
                    stream.streamTweets();
                    stream.close();
                } catch (final SQLException e) {
                    System.err.println("Could not connect to database");
                } // catch
            } // else
        } else {
            try {
                final StreamingApi stream = StreamingApiFilter.getInstance();
                stream.streamTweets();
                stream.close();
            } catch (final SQLException e) {
                System.err.println("Could not connect to database");
            } // catch
        } // else
    } // main(String[])
} // Main