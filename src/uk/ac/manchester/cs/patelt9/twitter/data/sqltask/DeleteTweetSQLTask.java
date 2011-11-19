package uk.ac.manchester.cs.patelt9.twitter.data.sqltask;

import uk.ac.manchester.cs.patelt9.twitter.data.SqlConnector;

public class DeleteTweetSQLTask implements SQLTask {
    private final long id;

    public DeleteTweetSQLTask(final long id) {
        this.id = id;
    } // DeleteTweetIdSQLTask(long)

    @Override
    public int doSqlTask(final SqlConnector sql) {
        return sql.deleteTweet(id);
    } // doSqlTask(SqlConnector)

    @Override
    public String toString() {
        return Long.toString(id);
    } // toString()

} // DeleteTweetIdSQLTask
