package uk.ac.manchester.cs.patelt9.twitter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLException;

import uk.ac.manchester.cs.patelt9.twitter.data.SentimentAnalysis;
import uk.ac.manchester.cs.patelt9.twitter.data.SqlConnector;
import uk.ac.manchester.cs.patelt9.twitter.parse.ScannerThread;
import uk.ac.manchester.cs.patelt9.twitter.parse.StreamParseThread;
import uk.ac.manchester.cs.patelt9.twitter.stream.StreamingApi;

public class RunAll {
    public static void main(final String[] args) {
        final SqlConnector sql;
        try {
            sql = SqlConnector.getInstance();
            final SentimentAnalysis sa = SentimentAnalysis.getInstance(sql);

            final StreamThread streamThread = new StreamThread(args, sql);

            final ScannerThread scanner = new ScannerThread() {
                @Override
                protected void performTask() {
                    sa.close();
                    streamThread.interrupt();
                    try {
                        streamThread.join();
                    } catch (final InterruptedException e) {
                        e.printStackTrace();
                    } // catch
                    sql.close();
                    System.exit(0);
                } // performTask()
            };
            scanner.start();
            streamThread.start();
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
        private StreamParseThread p = null;

        public StreamThread(final String[] args, final SqlConnector sql) throws SQLException {
            this("Stream", args, sql);
        } // StreamThread(String[], SqlConnector)

        public StreamThread(final String s, final String[] args, final SqlConnector sql)
                throws SQLException {
            super(s);
            stream = Stream.getStream(args, sql);
        } // StreamThread(String, String[], SqlConnector)

        public final void run() {
            try {
                stream.connect();
                try {
                    stream.initialiseReader();
                    while (!isInterrupted()) {
                        p = stream.streamTweet();
                    } // while
                } catch (final IOException e) {
                    e.printStackTrace();
                    return;
                } finally {
                    if (p != null) {
                        p.removeListener(stream);
                    } // if
                    stream.close();
                } // finally
            } catch (final MalformedURLException e) {
                System.err.println("Error parsing URL");
                interrupt();
            } catch (final IOException e) {
                System.err.println("Could not connect to server");
                interrupt();
            } // catch
        } // run()

        @Override
        public void interrupt() {
            if (p != null) {
                try {
                    p.join();
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                } // catch
            } // if
            super.interrupt();
        } // interrupt()
    } // StreamThread
} // AnalyseSentiment
