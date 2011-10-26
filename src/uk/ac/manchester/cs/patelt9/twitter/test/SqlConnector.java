package uk.ac.manchester.cs.patelt9.twitter.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

public class SqlConnector {
    private static final String JDBC = "com.mysql.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/Twitter";
    public static final int DB_ERROR = -1;

    private static String dbUser = null, dbPass = null;

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
            Class.forName(JDBC);
            try {
                con = DriverManager.getConnection(DB_URL, dbUser, dbPass);
            } catch (final SQLException e) {
                e.printStackTrace();
            } // catch
        } catch (final ClassNotFoundException e) {
            e.printStackTrace();
        } // catch
    } // SqlConnector()

    public int insertUser(final long id) {
        return insert("INSERT INTO user VALUES(default, '" + Long.toString(id) + "');");
    } // insertUser(long)

    private int insert(final String sqlStatement){
        return executeUpdate(sqlStatement);
    } // insert

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

    public int insertTweet(final long id, final String content, final String createdAt) {
        insertUser(id);
        return insert("INSERT INTO tweet VALUES(default, '" + content + "', '" + createdAt + "', '"
                + Long.toString(id) + "');");
    } // insertTweet(long, String, String)

    public int deleteAll() {
        final int i = executeUpdate("DELETE FROM tweet");
        final int j = executeUpdate("DELETE FROM user");
        if (i == DB_ERROR || j == DB_ERROR){
            return DB_ERROR;
        } else {
            return i+j;
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