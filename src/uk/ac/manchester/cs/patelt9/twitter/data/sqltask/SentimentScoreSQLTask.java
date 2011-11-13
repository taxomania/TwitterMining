package uk.ac.manchester.cs.patelt9.twitter.data.sqltask;

import uk.ac.manchester.cs.patelt9.twitter.data.SqlConnector;

public class SentimentScoreSQLTask extends SentimentSQLTask {
    private final String sentimentScore;

    public SentimentScoreSQLTask(final long id, final String s, final String sScore) {
        super(id, s);
        sentimentScore = sScore;
    } // SentimentScoreSQLTask(long, String, String)

    @Override
    public int doSqlTask(final SqlConnector sql) {
        return sql.updateSentimentScore(getSentiment(), sentimentScore, getId());
    } // doSqlTask(SqlConnector)

    @Override
    public String toString() {
        return super.toString() + ": " + sentimentScore;
    } // toString()
} // SentimentScoreSQLTask