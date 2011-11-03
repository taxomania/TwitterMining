package uk.ac.manchester.cs.patelt9.twitter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLException;

import uk.ac.manchester.cs.patelt9.twitter.data.SentimentAnalysis;
import uk.ac.manchester.cs.patelt9.twitter.data.SqlConnector;
import uk.ac.manchester.cs.patelt9.twitter.stream.StreamingApi;
import uk.ac.manchester.cs.patelt9.twitter.stream.StreamingApiFilterPost;
import uk.ac.manchester.cs.patelt9.twitter.stream.StreamingApiSample;

public class Main {
    public static void main(final String[] args) {
        if (args.length != 0) {
            if (args[0].equals("delete")) {
                try {
                    final SqlConnector sql = SqlConnector.getInstance();
                    System.out.println(sql.deleteAll() + " rows deleted");
                    sql.close();
                } catch (final SQLException e) {
                    System.err.println("Failed to delete tables");
                    System.exit(1);
                } // catch
            } else if (args[0].equals("error")) { // Delete all tweets returning error from
                                                  // sentiment analysis
                try {
                    final SqlConnector sql = SqlConnector.getInstance();
                    System.out.println(sql.deleteError() + " tweets deleted");
                    sql.close();
                } catch (final SQLException e) {
                    System.err.println("Failed to delete tweets");
                    System.exit(1);
                } // catch
            } else if (args[0].equals("analyse")) {
                try {
                    final SentimentAnalysis sa = SentimentAnalysis.getInstance();
                    sa.loadDataSet();
                    sa.analyseSentiment();
                } catch (final SQLException e) {
                    System.err.println("Could not connect to database");
                    System.exit(1);
                } catch (final IOException e) {
                    System.err.println("Could not find API key");
                    System.exit(1);
                } // catch
            } else {
                try {
                    final StreamingApi stream;
                    if (args[0].equals("sample")) {
                        stream = StreamingApiSample.getInstance();
                    } else {
                        stream = StreamingApiFilterPost.getInstance(args);
                    } // else
                    try {
                        stream.connect();
                    } catch (final MalformedURLException e) {
                        System.err.println("Error parsing URL");
                        System.exit(1);
                    } catch (final IOException e) {
                        System.err.println("Could not connect to server");
                        System.exit(1);
                    } // catch
                    stream.streamTweets();
                } catch (final SQLException e) {
                    System.err.println("Could not connect to database");
                    System.exit(1);
                } // catch
            } // else
        } else {
            try {
                final StreamingApiFilterPost stream = StreamingApiFilterPost.getInstance();
                try {
                    stream.connect();
                } catch (final MalformedURLException e) {
                    System.err.println("Error parsing URL");
                    System.exit(1);
                } catch (final IOException e) {
                    System.err.println("Could not connect to server");
                    System.exit(1);
                } // catch
                stream.streamTweets();
            } catch (final SQLException e) {
                System.err.println("Could not connect to database");
                System.exit(1);
            } // catch
        } // else
    } // main(String[])
} // Main