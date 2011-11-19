package uk.ac.manchester.cs.patelt9.twitter.data;

import uk.ac.manchester.cs.patelt9.twitter.data.task.DatabaseTask;

public abstract class DatabaseThread extends Thread {
    protected abstract void performTask();

    public DatabaseThread(final String s) {
        super(s);
    } // DatabaseThread(String)

    public abstract boolean addTask(DatabaseTask task);
} // DatabaseThread
