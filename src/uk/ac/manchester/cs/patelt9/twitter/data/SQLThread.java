package uk.ac.manchester.cs.patelt9.twitter.data;

import java.sql.SQLException;

/**
 * Create a new Thread to carry out all MySQL tasks in a queue
 *
 * @author Tariq Patel
 */
public final class SQLThread extends DatabaseThread {
    /**
     * Class constructor
     *
     * @throws SQLException
     */
    public SQLThread() throws SQLException {
        this("SQL");
    } // SQLThread()

    /**
     * Class constructor taking the Thread's name as a parameter.
     * <p>
     * Passes an instance of SQLConnector to its superclass
     *
     * @param s
     *            Thread name
     *
     * @throws SQLException
     * @see SqlConnector
     */
    public SQLThread(final String s) throws SQLException {
        super(s, SqlConnector.getInstance());
    } // SQLThread(String)
} // SQLThread