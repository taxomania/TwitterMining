package uk.ac.manchester.cs.patelt9.twitter.data.sql;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.ac.manchester.cs.patelt9.twitter.data.SqlConnector;

public class SQLThread extends Thread {
    private final SqlConnector sql;
    private int affectedRows;

    public SQLThread() throws SQLException {
        this("SQL");
    } // SQLThread()

    public SQLThread(final String s) throws SQLException {
        super(s);
        sql = SqlConnector.getInstance();
        affectedRows = 0;
    } // SQLThread(String)

    public interface SqlTaskCompleteListener {
        void onSqlTaskComplete(int rowsAffected);
    } // SQLTaskCompleteListener

    private static final Set<SqlTaskCompleteListener> listeners = new HashSet<SqlTaskCompleteListener>();

    protected final void notifyListeners(final int rowsAffected) {
        synchronized (listeners) {
            for (final SqlTaskCompleteListener listener : listeners) {
                listener.onSqlTaskComplete(rowsAffected);
            } // for
        } // synchronized
    } // notifyListeners(int)

    public final void addListener(final SqlTaskCompleteListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        } // synchronized
    } // addListener(SqlTaskCompleteListener)

    public final void removeListener(final SqlTaskCompleteListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        } // synchronized
    } // removeListener(SqlTaskCompleteListener)

    private final List<SQLTask> taskList = new ArrayList<SQLTask>();

    @Override
    public final void run() {
        while (!isInterrupted()) {
            try {
                performTask(taskList.remove(0));
            } catch (final IndexOutOfBoundsException e) {
                //try {
                  //  Thread.sleep(1000);
                    continue;
                //} catch (final InterruptedException e1) {}
            } // catch
        } // while
    } // run()

    @Override
    public void interrupt() {
        while (!taskList.isEmpty()) {
            performTask(taskList.remove(0));
        } // while
        sql.close();
        System.out.println(affectedRows);
        // notifyListeners(affectedRows);
        super.interrupt();
    } // interrupt()

    protected void performTask(final SQLTask task) {
        affectedRows += task.doSqlTask(sql);
    } // performTask(SQLTask)

    public boolean addTask(final SQLTask task){
        return taskList.add(task);
    } // addTask(SQLTask)
} // SQLThread