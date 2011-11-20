package uk.ac.manchester.cs.patelt9.twitter.data;

import java.util.ArrayList;
import java.util.List;

import uk.ac.manchester.cs.patelt9.twitter.data.task.DatabaseTask;

public abstract class DatabaseThread extends Thread {
    private final List<DatabaseTask> taskList = new ArrayList<DatabaseTask>();
    private int affectedRows = 0;
    private DatabaseConnector db;

    public DatabaseThread(final String s, final DatabaseConnector connector) {
        super(s);
        db = connector;
    } // DatabaseThread(String)

    @Override
    public final void run() {
        while (!isInterrupted()) {
            try {
                synchronized (taskList) {
                    performTask();
                } // synchronized
            } catch (final IndexOutOfBoundsException e) {
                continue;
            } // catch
        } // while
    } // run()

    @Override
    public void interrupt() {
        synchronized (taskList) {
            while (!taskList.isEmpty()) {
                performTask();
            } // while
        } // synchronized
        db.close();
        System.out.println(affectedRows + " rows affected");
        super.interrupt();
    } // interrupt()

    private void performTask() {
        affectedRows += taskList.remove(0).doTask(db);
    } // performTask()

    public boolean addTask(final DatabaseTask task) {
        synchronized (taskList) {
            return taskList.add(task);
        } // synchronized
    } // addTask(DatabaseTask)
} // DatabaseThread