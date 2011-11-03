package uk.ac.manchester.cs.patelt9.twitter;

import java.io.IOException;
import java.sql.SQLException;

import uk.ac.manchester.cs.patelt9.twitter.data.SentimentAnalysis;
import uk.ac.manchester.cs.patelt9.twitter.data.SqlConnector;
import uk.ac.manchester.cs.patelt9.twitter.parse.ScannerThread;

public class AnalyseSentiment {
    public static void main(final String[] args) {
        final SqlConnector sql;
        try {
            sql = SqlConnector.getInstance();
            final SentimentAnalysis sa = SentimentAnalysis.getInstance(sql);
            final ScannerThread scanner = new ScannerThread() {
                @Override
                protected void performTask() {
                    sa.close();
                    sql.close();
                    System.exit(0);
                } // performTask()
            };
            scanner.start();
            sa.loadDataSet();
            sa.analyseSentiment();
            scanner.interrupt();
        } catch (final SQLException e) {
            System.err.println("Could not connect to database");
        } catch (final IOException e) {
            System.err.println("Could not find API key");
        } // catch
    } // main(String[])

} // AnalyseSentiment