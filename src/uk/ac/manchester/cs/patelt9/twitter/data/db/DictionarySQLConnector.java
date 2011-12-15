package uk.ac.manchester.cs.patelt9.twitter.data.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
    private PreparedStatement insertSoftware = null;
    private PreparedStatement insertCompany = null;
    private PreparedStatement insertKeyword = null;
    private PreparedStatement dictType = null;

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
                "?, "       +  // software_name
                "?"         +  // type [software | game | prog_lang | os] // AS INT
                ");");
        insertSoftware = con.prepareStatement(
                "INSERT INTO dictionary VALUES(" +
                "default, " +  // id
                "?, "       +  // software_name
                "default"   +  // type
                ");");

        insertCompany = con.prepareStatement(
                "INSERT INTO company VALUES(" +
                "default, " +  // id
                "?"       +  // name
                ");");

        insertKeyword = con.prepareStatement(
                "INSERT INTO keyword VALUES(" +
                "default, " +  // id
                "?"         +  // word
                ");");
        dictType = con.prepareStatement("SELECT id FROM dict_type WHERE type=?");
        // @formatter:on
    } // DictionarySQLConnector()

    /**
     * Insert a new software tool into the dictionary
     *
     * @param softwareName
     *            The word to be inserted
     * @return The number of affected rows or -1 if an error occurs
     */
    public int insert(final String[] name) {
        if (name.length == 1) { return insertSoftware(name[0]); }
        try {
            insert.setString(1, name[0]);
            insert.setInt(2, getType(name[1]));
            return executeUpdate(insert);
        } catch (final SQLException e) {
            e.printStackTrace();
            return DB_ERROR;
        } // catch
    } // insert(String[])

    private int getType(final String type) throws SQLException {
        dictType.setString(1, type);
        final ResultSet s = dictType.executeQuery();
        if (!s.first()) { throw new SQLException("Type not found"); }
        return s.getInt(1);
    } // getType(String)

    private int insertSoftware(final String name) {
        try {
            insertSoftware.setString(1, name);
            return executeUpdate(insertSoftware);
        } catch (final SQLException e) {
            e.printStackTrace();
            return DB_ERROR;
        } // catch
    } // insertSoftware(String)

    public int insertCompany(final String name) {
        try {
            insertCompany.setString(1, name);
            return executeUpdate(insertCompany);
        } catch (final SQLException e) {
            e.printStackTrace();
            return DB_ERROR;
        } // catch
    } // insertCompany

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
        return executeUpdate("TRUNCATE TABLE dictionary");
    } // deleteAll()

    public ResultSet selectAll() {
        try {
            //@formatter:off
            return con.createStatement().executeQuery(
                    "SELECT software_name FROM dictionary UNION " +
                    "SELECT name FROM company UNION " +
                    "SELECT word FROM keyword");
            //@formatter:on
        } catch (final SQLException e) {
            e.printStackTrace();
            return null;
        } // catch
    } // selectAll()

    @Override
    public void close() {
        super.close();
        mySql = null;
    } // close()
} // DictionarySQLConnector