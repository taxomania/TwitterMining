package uk.ac.manchester.cs.patelt9.twitter.data.sqltask;

import uk.ac.manchester.cs.patelt9.twitter.data.SqlConnector;

public abstract class SQLTask {
    public abstract int doSqlTask(SqlConnector sql);
    public abstract String toString();
} // SQLTask