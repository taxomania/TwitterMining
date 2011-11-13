package uk.ac.manchester.cs.patelt9.twitter.practice;

import java.util.HashSet;
import java.util.Set;

public abstract class SqlThread extends Thread {
    public SqlThread() {
        this("SQL");
    } // SqlThread()

    public SqlThread(final String s) {
        super(s);
    } // SqlThread(String)

    public interface SqlTaskCompleteListener {
        void onSqlTaskComplete(int rowsAffected);
    } // ParseListener

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

    @Override
    public final void run() {
        performTask();
    } // run()

    protected abstract void performTask();
} // SqlThread
