package uk.ac.manchester.cs.patelt9.twitter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

public class SqlConnector {
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/Twitter";
    public static final int DB_ERROR = -1;

    private static String dbUser = null, dbPass = null;

    private PreparedStatement insertUser = null;
    private PreparedStatement insertTweet = null;

    static {
        getUserPass();
    } // static

    private Connection con = null;
    private static SqlConnector mySql = null;

    // Singleton lock on database helper
    public static SqlConnector getInstance() {
        if (mySql == null) {
            mySql = new SqlConnector();
        } // if
        return mySql;
    } // getInstance()

    private SqlConnector() {
        try {
            Class.forName(JDBC_DRIVER);
            try {
                con = DriverManager.getConnection(DB_URL, dbUser, dbPass);
                // @formatter:off
                insertUser = con.prepareStatement(
                        "INSERT INTO user VALUES(default, " +
                        "?" +  // Id
                        ");");

                insertTweet = con.prepareStatement(
                        "INSERT INTO tweet VALUES(default, " +
                        "?, " + // Content
                        "?, " + // Created_at
                        "?"   + // Id
                        ");");
                // @formatter:on
            } catch (final SQLException e) {
                e.printStackTrace();
            } // catch
        } catch (final ClassNotFoundException e) {
            e.printStackTrace();
        } // catch
    } // SqlConnector()

    private int executeUpdate(final PreparedStatement s) {
        try {
            return s.executeUpdate();
        } catch (final MySQLIntegrityConstraintViolationException e) {
            // System.err.println(e.getMessage());
            return DB_ERROR;
        } catch (SQLException e) {
            e.printStackTrace();
            return DB_ERROR;
        } // catch
    } // insert(PreparedStatement)

    public int insertUser(final long id) {
        try {
            insertUser.setLong(1, id);
            return executeUpdate(insertUser);
        } catch (final SQLException e) {
            e.printStackTrace();
            return DB_ERROR;
        } // catch
    } // insertUser(long)

    public int insertTweet(final long id, final String content, final String createdAt) {
        insertUser(id);
        try {
            insertTweet.setString(1, content);
            insertTweet.setString(2, createdAt);
            insertTweet.setLong(3, id);
            return executeUpdate(insertTweet);
        } catch (final SQLException e) {
            e.printStackTrace();
            return DB_ERROR;
        } // catch
    } // insertUser(long)

    private int executeUpdate(final String sqlStatement) {
        try {
            final Statement s = con.createStatement();
            try {
                return s.executeUpdate(sqlStatement);
            } catch (final MySQLIntegrityConstraintViolationException e) {
                System.err.println(e.getMessage());
                return DB_ERROR;
            } // catch
        } catch (final SQLException e) {
            e.printStackTrace();
            return DB_ERROR;
        } // catch
    } // insert(String)

    public int deleteAll() {
        final int i = executeUpdate("DELETE FROM tweet");
        final int j = executeUpdate("DELETE FROM user");
        if (i == DB_ERROR || j == DB_ERROR) {
            return DB_ERROR;
        } else {
            return i + j;
        } // else
    } // deleteAll()

    public void close() {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } // catch
        } // if
    } // close()

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

} // SqlConnector