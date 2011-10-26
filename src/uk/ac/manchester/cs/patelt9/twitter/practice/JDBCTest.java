package uk.ac.manchester.cs.patelt9.twitter.practice;

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

public class JDBCTest {
    private static final String JDBC = "com.mysql.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/Twitter";
    public static final int DB_ERROR = -1;

    private static String dbUser = null, dbPass = null;

    static {
        getUserPass();
    } // static

    private Connection con = null;
    private static JDBCTest mySql = null;

    // Singleton lock on database helper
    public static JDBCTest getInstance() {
        if (mySql == null) {
            mySql = new JDBCTest();
        }
        return mySql;
    } // getInstance()

    private JDBCTest() {
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
    } // JDBCTest()

    public int insertUser(final long id) {
        try {
            final Statement s = con.createStatement();
            try {
                return s.executeUpdate("INSERT INTO user VALUES(default, '" + Long.toString(id)
                        + "');");
            } catch (final MySQLIntegrityConstraintViolationException e) {
                System.err.println(e.getMessage());
                return DB_ERROR;
            } // catch
        } catch (final SQLException e) {
            e.printStackTrace();
            return DB_ERROR;
        }
    } // insertUser(long)

    public void close() {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } // catch
        } // if
    } // close()

    // This method is for testing purposes only
    private void test() {
        try {
            Class.forName(JDBC);
            try {
                con = DriverManager.getConnection(DB_URL, dbUser, dbPass);
                final Statement s = con.createStatement();
                try {
                    final int i = s.executeUpdate("INSERT INTO user VALUES(default, '251463096');");
                    System.out.println(Integer.toString(i));
                } catch (final MySQLIntegrityConstraintViolationException e) {
                    System.err.println(e.getMessage());
                } // catch
            } catch (final SQLException e) {
                e.printStackTrace();
            } finally {
                if (con != null) {
                    try {
                        con.close();
                    } catch (final SQLException e) {
                        e.printStackTrace();
                    } // catch
                } // if
            } // finally
        } catch (final ClassNotFoundException e) {
            e.printStackTrace();
        } // catch
    } // test()

    // For testing purposes
    public static void main(final String[] args) {
        new JDBCTest().test();
    } // main(String[])

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

} // JDBCTest