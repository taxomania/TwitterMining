package uk.ac.manchester.cs.patelt9.twitter.data.sqltask;

import uk.ac.manchester.cs.patelt9.twitter.data.SqlConnector;

public class DeleteTweetIdSQLTask extends DeleteTweetSQLTask {
    public DeleteTweetIdSQLTask(final long id) {
        super(id);
    } // DeleteTweetIdSQLTask(long)

    @Override
    public int doSqlTask(final SqlConnector sql) {
        return sql.deleteTweetByTweetId(getId());
    } // doSqlTask(SqlConnector)
} // DeleteTweetIdSQLTask
