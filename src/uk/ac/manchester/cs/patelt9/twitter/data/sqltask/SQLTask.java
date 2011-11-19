package uk.ac.manchester.cs.patelt9.twitter.data.sqltask;

import uk.ac.manchester.cs.patelt9.twitter.data.SqlConnector;
import uk.ac.manchester.cs.patelt9.twitter.data.task.DatabaseTask;

public interface SQLTask extends DatabaseTask {
    public int doSqlTask(SqlConnector sql);
} // SQLTask