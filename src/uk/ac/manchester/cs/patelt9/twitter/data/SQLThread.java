package uk.ac.manchester.cs.patelt9.twitter.data;

import java.sql.SQLException;

public final class SQLThread extends DatabaseThread {
    public SQLThread() throws SQLException {
        this("SQL");
    } // SQLThread()

    public SQLThread(final String s) throws SQLException {
        super(s, SqlConnector.getInstance());
    } // SQLThread(String)
} // SQLThread