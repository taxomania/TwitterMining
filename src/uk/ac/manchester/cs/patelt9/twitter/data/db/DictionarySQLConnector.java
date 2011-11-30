package uk.ac.manchester.cs.patelt9.twitter.data.db;

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
    private PreparedStatement insertKeyword = null;

    /**
     * Retrieve the current instance of DictionarySQLConnector, or create a new one if it is null;
     *
     * @return A single instance of DictionarySQLConnector
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
        insertKeyword = con.prepareStatement(
                "INSERT INTO keyword VALUES(" +
                "default, " +  // id
                "?"   +  // word
                ");");
        // @formatter:on
    } // DictionarySQLConnector()

    /**
     * Insert a new software tool into the dictionary
     *
     * @param softwareName
     *            The word to be inserted
     * @return The number of affected rows or -1 if an error occurs
     */
    public int insert(final String softwareName) {
        try {
            insert.setString(1, softwareName);
            return executeUpdate(insert);
        } catch (final SQLException e) {
            e.printStackTrace();
            return DB_ERROR;
        } // catch
    } // insert(String)

    public int insertKeyword(final String word) {
        try {
            insertKeyword.setString(1, word);
            return executeUpdate(insertKeyword);
        } catch (final SQLException e) {
            e.printStackTrace();
            return DB_ERROR;
        } // catch
    } // insertKeyword(String)

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