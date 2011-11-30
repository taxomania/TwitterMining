package uk.ac.manchester.cs.patelt9.twitter.data.db.task;

import uk.ac.manchester.cs.patelt9.twitter.data.db.DatabaseConnector;

/**
 * Any database tasks to be carried out must implement this interface.
 *
 * @author Tariq Patel
 */
public interface DatabaseTask {
    /**
     *
     * @return String representation of task
     */
    public String toString();

    /**
     * Carry out the task specific to the implementing task
     *
     * @param db
     *            DatabaseConnector object
     * @return The number of rows affected
     */
    public int doTask(DatabaseConnector db);
} // DatabaseTask
