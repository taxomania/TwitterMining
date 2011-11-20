package uk.ac.manchester.cs.patelt9.twitter.data.task.sql;

import uk.ac.manchester.cs.patelt9.twitter.data.SqlConnector;
import uk.ac.manchester.cs.patelt9.twitter.data.task.DeleteTask;

public class DeleteTweetSQLTask extends DeleteTask implements SQLTask {
    public DeleteTweetSQLTask(final long id) {
        super(id);
    } // DeleteTweetIdSQLTask(long)

    @Override
    public int doSqlTask(final SqlConnector sql) {
        return sql.deleteTweet(getId());
    } // doSqlTask(SqlConnector)
} // DeleteTweetIdSQLTask
