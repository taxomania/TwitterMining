package uk.ac.manchester.cs.patelt9.twitter.data.sqltask;

import uk.ac.manchester.cs.patelt9.twitter.data.SqlConnector;

public class SentimentSQLTask extends SQLTask {
    private final long id;
    private final String sentiment;

    public SentimentSQLTask(final long id, final String s) {
        this.id = id;
        sentiment = s;
    } // SentimentSQLTask(long, String)

    @Override
    public int doSqlTask(final SqlConnector sql) {
        return sql.updateSentiment(sentiment, id);
    } // doSqlTask(SqlConnector)

    public long getId() {
        return id;
    } // getId()

    public String getSentiment() {
        return sentiment;
    } // getSentiment()

    @Override
    public String toString() {
        return Long.toString(id) + ": " + sentiment;
    } // toString()
} // SentimentSQLTask
