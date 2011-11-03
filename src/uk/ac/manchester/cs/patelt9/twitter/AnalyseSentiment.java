package uk.ac.manchester.cs.patelt9.twitter;

import java.io.IOException;
import java.sql.SQLException;

import uk.ac.manchester.cs.patelt9.twitter.data.SentimentAnalysis;

public class AnalyseSentiment {
    public static void main(final String[] args){
        try {
            final SentimentAnalysis sa = SentimentAnalysis.getInstance();
            sa.loadDataSet();
            sa.analyseSentiment();
            SqlTasks.main(null);
        } catch (final SQLException e) {
            System.err.println("Could not connect to database");
            System.exit(1);
        } catch (final IOException e) {
            System.err.println("Could not find API key");
            System.exit(1);
        } // catch
    } // main(String[])
} // AnalyseSentiment
