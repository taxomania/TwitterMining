package uk.ac.manchester.cs.patelt9.twitter.data.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
        getUserPass();
    } // static

    private static void getUserPass() {
        BufferedReader userPass = null;
        try {
            userPass = new BufferedReader(new FileReader(new File("sqluserpass.txt")));
            dbUser = userPass.readLine();
            dbPass = userPass.readLine();
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
            System.err.println("Login file not found");
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            if (userPass != null) {
                try {
                    userPass.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                } // catch
            } // if
        } // finally
    } // getUserPass()

    protected SQLConnector() throws SQLException {
        if (con == null) {
            try {
                Class.forName(JDBC_DRIVER);
                con = DriverManager.getConnection(DB_URL, dbUser, dbPass);
                System.out.println("Connecting"); // TEST
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
        System.out.println("CLOSED"); // TEST
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

    protected Connection getConnection() {
        return con;
    } // getConnection()
} // SQLConnector
