package uk.ac.manchester.cs.patelt9.twitter.data.sqltask;

import uk.ac.manchester.cs.patelt9.twitter.data.SqlConnector;

public class DeleteTweetSQLTask extends SQLTask {
    private final long id;

    public DeleteTweetSQLTask(final long id) {
        this.id = id;
    } // DeleteSQLTask(long)

    public long getId() {
        return id;
    } // getId()

    @Override
    public int doSqlTask(final SqlConnector sql) {
        return sql.deleteTweetById(id);
    } // doSqlTask(SqlConnector)

    @Override
    public String toString() {
        return Long.toString(id);
    } // toString()

} // DeleteTweetSQLTask