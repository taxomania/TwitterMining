package uk.ac.manchester.cs.patelt9.twitter.data;

import java.util.ArrayList;
import java.util.List;

import uk.ac.manchester.cs.patelt9.twitter.data.task.DatabaseTask;

/**
 * Create a queue of DatabaseTask objects to be performed.
 *
 * All database task queueing threads must extend this class.
 *
 * @author Tariq Patel
 *
 */
public abstract class DatabaseThread extends Thread {
    private final List<DatabaseTask> taskList = new ArrayList<DatabaseTask>();
    private int affectedRows = 0;
    private TweetDatabaseConnector db;

    /**
     * Constructor taking thread name and a DatabaseConnector object
     *
     * @param s
     *            Thread name
     * @param connector
     *            DatabaseConnector object
     */
    public DatabaseThread(final String s, final TweetDatabaseConnector connector) {
        super(s);
        db = connector;
    } // DatabaseThread(String)

    /**
     * Call doTask() method for each DatabaseTask in the queue.
     */
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

    /**
     * Clear queue and close database.
     */
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

    /**
     * Add a DatabaseTask to the queue.
     *
     * @param task
     *            DatabaseTask object to be added to queue
     * @return true on successfully adding task to queue
     */
    public boolean addTask(final DatabaseTask task) {
        synchronized (taskList) {
            return taskList.add(task);
        } // synchronized
    } // addTask(DatabaseTask)
} // DatabaseThread