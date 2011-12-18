package uk.ac.manchester.cs.patelt9.twitter.data.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import uk.ac.manchester.cs.patelt9.twitter.StaticFunctions;

import com.mysql.jdbc.MysqlDataTruncation;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

/**
 * Abstract helper class to connect to MySQL database
 *
 * @author Tariq Patel
 *
 */
public abstract class SQLConnector {
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/TwitterMining";
    public static final int DB_ERROR = -1;

    private static String dbUser = null, dbPass = null;

    private static Connection con = null; // Persistent state

    static {
        final String[] details = StaticFunctions.getDetails("sqluserpass.txt").split(":");
        try {
            dbUser = details[0];
            dbPass = details[1];
        } catch (final ArrayIndexOutOfBoundsException e) {
            System.err.println("Error with login details");
        } // catch
    } // static

    protected SQLConnector() throws SQLException {
        if (con == null) {
            try {
                Class.forName(JDBC_DRIVER);
                con = DriverManager.getConnection(DB_URL, dbUser, dbPass);
            } catch (final ClassNotFoundException e) {
                e.printStackTrace();
            } // catch
        } // if
    } // SQLConnector()

    protected int executeUpdate(final PreparedStatement s) {
        try {
            return s.executeUpdate();
        } catch (final MysqlDataTruncation e) {
            System.err.println(e.getMessage());
            return DB_ERROR;
        } catch (final MySQLIntegrityConstraintViolationException e) {
            // System.err.println(e.getMessage());
            return DB_ERROR;
        } catch (final SQLException e) {
            e.printStackTrace();
            return DB_ERROR;
        } // catch
    } // executeUpdate(PreparedStatement)

    protected int executeUpdate(final String sqlStatement) {
        try {
            return con.createStatement().executeUpdate(sqlStatement);
        } catch (final MySQLIntegrityConstraintViolationException e) {
            System.err.println(e.getMessage());
            return DB_ERROR;
        } catch (final SQLException e) {
            e.printStackTrace();
            return DB_ERROR;
        } // catch
    } // executeUpdate(String)

    public void close() {
        if (con != null) {
            try {
                con.close();
                con = null;
            } catch (final SQLException e) {
                e.printStackTrace();
            } // catch
        } // if
    } // close()

    public abstract int deleteAll();

    protected final Connection getConnection() {
        return con;
    } // getConnection()
} // SQLConnector
