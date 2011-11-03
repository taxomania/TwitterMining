package uk.ac.manchester.cs.patelt9.twitter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLException;

import uk.ac.manchester.cs.patelt9.twitter.data.SentimentAnalysis;
import uk.ac.manchester.cs.patelt9.twitter.data.SqlConnector;
import uk.ac.manchester.cs.patelt9.twitter.parse.ScannerThread;
import uk.ac.manchester.cs.patelt9.twitter.stream.StreamingApi;
import uk.ac.manchester.cs.patelt9.twitter.stream.StreamingApiFilterPost;
import uk.ac.manchester.cs.patelt9.twitter.stream.StreamingApiSample;

public class AnalyseSentiment {
    public static void main(final String[] args) {
        final SqlConnector sql;
        try {
            sql = SqlConnector.getInstance();
            final SentimentAnalysis sa = SentimentAnalysis.getInstance(sql);

          //  final StreamThread streamThread = new StreamThread(args, sql);

            final ScannerThread scanner = new ScannerThread() {
                @Override
                protected void performTask() {
                    sa.close();
            //        streamThread.interrupt();
                    SqlTasks.delete(null, sql);
                    sql.close();
                    System.exit(0);
                } // performTask()
            };
            scanner.start();
            //streamThread.start();
            sa.loadDataSet();
            sa.analyseSentiment();
            scanner.interrupt();
        } catch (final SQLException e) {
            System.err.println("Could not connect to database");
        } catch (final IOException e) {
            System.err.println("Could not find API key");
        } // catch
    } // main(String[])

    private static final class StreamThread extends Thread {
        final StreamingApi stream;

        public StreamThread(final String[] args, final SqlConnector sql) {
            this("Stream", args, sql);
        } // StreamThread(String[], SqlConnector)

        public StreamThread(final String s, final String[] args, final SqlConnector sql) {
            super(s);
            if (args.length != 0) {
                if (args[0].equals("sample")) {
                    stream = StreamingApiSample.getInstance(sql);
                } else {
                    stream = StreamingApiFilterPost.getInstance(sql, args);
                } // else
            } else {
                stream = StreamingApiFilterPost.getInstance(sql);
            } // else
        } // StreamThread(String, String[], SqlConnector)

        public final void run() {
            try {
                stream.connect();
                stream.initialiseReader();
                while (!isInterrupted()) {
                    stream.streamTweet();
                } // while
                stream.close();
                // stream.streamTweets();
            } catch (final MalformedURLException e) {
                System.err.println("Error parsing URL");
                interrupt();
            } catch (final IOException e) {
                System.err.println("Could not connect to server");
                interrupt();
            } // catch
        } // run()

    } // StreamThread
} // AnalyseSentiment
