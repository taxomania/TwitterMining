package uk.ac.manchester.cs.patelt9.twitter.data;

import java.sql.SQLException;

import uk.ac.manchester.cs.patelt9.twitter.data.db.TweetSQLConnector;

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
        this(true);
    } // SQLThread()

    public SQLThread(final boolean print) throws SQLException {
        this("SQL", print);
    }

    /**
     * Class constructor taking the Thread's name as a parameter.
     * <p>
     * Passes an instance of SQLConnector to its superclass
     *
     * @param s
     *            Thread name
     *
     * @throws SQLException
     * @see TweetSQLConnector
     */
    public SQLThread(final String s) throws SQLException {
        this(s, true);
    } // SQLThread(String)

    public SQLThread(final String s, final boolean print) throws SQLException {
        this(s, TweetSQLConnector.getInstance(), print);
    } // SQLThread(String, boolean)

    public SQLThread(final String s, final TweetSQLConnector t, final boolean print)
            throws SQLException {
        super(s, t, print);
    } // SQLThread(String)
} // SQLThread
