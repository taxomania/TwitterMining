package uk.ac.manchester.cs.patelt9.twitter.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Helper class to connect to MySQL and carry out database operations on the dictionary.
 *
 * @author Tariq Patel
 *
 */
public final class DictionarySQLConnector extends SQLConnector {
    private static DictionarySQLConnector mySql = null;
    private final Connection con = getConnection();
    private PreparedStatement insert = null;

    /**
     * Retrieve the current instance of SQLConnector, or create a new one if it is null;
     *
     * @return A single instance of SQLConnector
     * @throws SQLException
     */
    public static synchronized DictionarySQLConnector getInstance() throws SQLException {
        if (mySql == null) {
            mySql = new DictionarySQLConnector();
        } // if
        return mySql;
    } // getInstance()

    private DictionarySQLConnector() throws SQLException {
        // @formatter:off
        insert = con.prepareStatement(
                "INSERT INTO dictionary VALUES(" +
                "default, " +  // id
                "?"   +  // software_name
                ");");
        // @formatter:on
    } // DictionarySQLConnector()

    public int insert(final String word) {
        try {
            insert.setString(1, word);
            return executeUpdate(insert);
        } catch (final SQLException e) {
            e.printStackTrace();
            return DB_ERROR;
        } // catch
    } // updateSentiment(long, String)

    @Override
    public int deleteAll() {
        return executeUpdate("DELETE FROM dictionary");
    } // deleteAll()

    @Override
    public void close() {
        super.close();
        mySql = null;
    } // close()
} // DictionarySQLConnector