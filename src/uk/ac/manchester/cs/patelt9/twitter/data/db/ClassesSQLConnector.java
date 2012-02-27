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
public final class ClassesSQLConnector extends SQLConnector {
    private static ClassesSQLConnector mySql = null;

    private final Connection con = getConnection();

    private PreparedStatement insert = null;
    private PreparedStatement reasonClass = null;

    /**
     * Retrieve the current instance of ClassesSQLConnector, or create a new one if it is null;
     *
     * @return A single instance of ClassesSQLConnector
     * @throws SQLException
     */
    public static synchronized ClassesSQLConnector getInstance() throws SQLException {
        if (mySql == null) {
            mySql = new ClassesSQLConnector();
        } // if
        return mySql;
    } // getInstance()

    private ClassesSQLConnector() throws SQLException {
        // @formatter:off
        insert = con.prepareStatement(
                "INSERT INTO class_words VALUES(" +
                "default, " +  // id
                "?, "       +  // term
                "?"         +  // class_id
                ");");
        reasonClass = con.prepareStatement("SELECT id FROM reason_class WHERE name=?");
        // @formatter:on
    } // ClassesSQLConnector()

    /**
     * Insert a new term into the table
     *
     * @param term
     *            The term to be inserted
     * @return The number of affected rows or -1 if an error occurs
     */
    public int insert(final String[] term) {
        if (term.length < 2) { return DB_ERROR; } // if
        try {
            insert.setString(1, term[0]);
            insert.setInt(2, getType(term[1]));
            return executeUpdate(insert);
        } catch (final SQLException e) {
            e.printStackTrace();
            return DB_ERROR;
        } // catch
    } // insert(String[])

    private int getType(final String type) throws SQLException {
        reasonClass.setString(1, type);
        final ResultSet s = reasonClass.executeQuery();
        if (!s.first()) { throw new SQLException("Type not found"); }
        return s.getInt(1);
    } // getType(String)

    @Override
    public int deleteAll() {
        return executeUpdate("TRUNCATE TABLE class_words");
    } // deleteAll()

    @Override
    public void close() {
        super.close();
        mySql = null;
    } // close()
} // DictionarySQLConnector
