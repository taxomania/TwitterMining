package uk.ac.manchester.cs.patelt9.twitter.data;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import uk.ac.manchester.cs.patelt9.twitter.data.sqltask.SQLTask;

public class SQLThread extends Thread {
    private final SqlConnector sql;
    private final List<SQLTask> taskList = new ArrayList<SQLTask>();
    private int affectedRows;

    public SQLThread() throws SQLException {
        this("SQL");
    } // SQLThread()

    public SQLThread(final String s) throws SQLException {
        super(s);
        sql = SqlConnector.getInstance();
        affectedRows = 0;
    } // SQLThread(String)

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
        sql.close();
        System.out.println(affectedRows + " rows affected");
        super.interrupt();
    } // interrupt()

    protected void performTask() {
        affectedRows += taskList.remove(0).doSqlTask(sql);
    } // performTask()

    public boolean addTask(final SQLTask task) {
        synchronized (taskList) {
            return taskList.add(task);
        } // synchronized
    } // addTask(SQLTask)
} // SQLThread