package uk.ac.manchester.cs.patelt9.twitter.data.task.sql;

import uk.ac.manchester.cs.patelt9.twitter.data.SqlConnector;
import uk.ac.manchester.cs.patelt9.twitter.data.task.SentimentTask;

public class SentimentSQLTask extends SentimentTask implements SQLTask {
    public SentimentSQLTask(final long id, final String s) {
        super(id, s);
    } // SentimentSQLTask(long, String)

    @Override
    public int doSqlTask(final SqlConnector sql) {
        return sql.updateSentiment(getId(), getSentiment());
    } // doSqlTask(SqlConnector)
} // SentimentSQLTask
