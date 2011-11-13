package uk.ac.manchester.cs.patelt9.twitter.data.sqltask;

import uk.ac.manchester.cs.patelt9.twitter.data.SqlConnector;

public class DeleteSQLTask extends SQLTask {
    private final long id;
    public DeleteSQLTask(final long id) {
        this.id = id;
    } // DeleteSQLTask(long)

    @Override
    public int doSqlTask(final SqlConnector sql) {
        return sql.deleteTweetByTweetId(id);
    } // doSqlTask(SqlConnector)

    @Override
    public String toString() {
        return Long.toString(id);
    } // toString()
} // DeleteSQLTask
