package uk.ac.manchester.cs.patelt9.twitter.data.sqltask;

import uk.ac.manchester.cs.patelt9.twitter.data.SqlConnector;

public interface SQLTask {
    public int doSqlTask(SqlConnector sql);
    public String toString();
} // SQLTask