package uk.ac.manchester.cs.patelt9.twitter.data.task;

import uk.ac.manchester.cs.patelt9.twitter.data.DatabaseConnector;

public interface DatabaseTask {
    public String toString();
    public int doTask(DatabaseConnector db);
} // DatabaseTask
