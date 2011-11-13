package uk.ac.manchester.cs.patelt9.twitter.data.sql;

import uk.ac.manchester.cs.patelt9.twitter.data.SqlConnector;

public abstract class SQLTask {
    protected abstract int doSqlTask(SqlConnector sql);
} // SQLTask