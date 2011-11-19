package uk.ac.manchester.cs.patelt9.twitter.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.mysql.jdbc.MysqlDataTruncation;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

public class SqlConnector {
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/TwitterMining";
    public static final int DB_ERROR = -1;

    private static String dbUser = null, dbPass = null;

    private PreparedStatement insertUser = null;
    private PreparedStatement insertFilteredTweet = null;
    private PreparedStatement insertTweet = null;
    private PreparedStatement deleteTweetByTweetId = null;
    private PreparedStatement updateSentiment = null;
    private PreparedStatement updateSentimentScore = null;

    static {
        getUserPass();
    } // static

    private Connection con = null;
    private static SqlConnector mySql = null;

    // Singleton lock on database helper
    public static synchronized SqlConnector getInstance() throws SQLException {
        if (mySql == null) {
            mySql = new SqlConnector();
        } // if
        return mySql;
    } // getInstance()

    private SqlConnector() throws SQLException {
        try {
            Class.forName(JDBC_DRIVER);
            con = DriverManager.getConnection(DB_URL, dbUser, dbPass);
            // @formatter:off
            insertUser = con.prepareStatement(
                    "INSERT INTO user VALUES(" +
                    "?, " +  // Id
                    "?"   +  // Username
                    ");");

            insertFilteredTweet = con.prepareStatement(
                    "INSERT INTO tweet VALUES(default, " + // Id
                    "?, " + // Tweet_Id
                    "?, " + // Text
                    "?, " + // Created_at
                    "?, " + // User_Id
                    "default, default, " + // Sentiment, Sentiment_score,
                    "?);"); // Keyword

            insertTweet = con.prepareStatement(
                    "INSERT INTO tweet VALUES(default, " + // Id
                    "?, " + // Tweet_Id
                    "?, " + // Text
                    "?, " + // Created_at
                    "?, " + // User_Id
                    "default, default, default);"); // Sentiment, Sentiment_score, Keyword

            deleteTweetByTweetId = con.prepareStatement("DELETE FROM tweet WHERE tweet_id=?;");

            updateSentiment = con.prepareStatement("UPDATE tweet SET sentiment=? WHERE id=?;");
            updateSentimentScore = con.prepareStatement("UPDATE tweet SET sentiment=?, " +
                    "sentiment_score=? WHERE id=?;");
            // @formatter:on
        } catch (final ClassNotFoundException e) {
            e.printStackTrace();
        } // catch
    } // SqlConnector()

    private int executeUpdate(final PreparedStatement s) {
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

    private int insertUser(final long id, final String screenName) throws SQLException {
        insertUser.setLong(1, id);
        insertUser.setString(2, screenName);
        return executeUpdate(insertUser);
    } // insertUser(long, String)

    public int insertTweet(final long id, final String screenName, final String content,
            final String createdAt, final long userId, final String keyword) {
        try {
            insertUser(userId, screenName);
            try {
                setTweetValues(insertFilteredTweet, id, content, createdAt, userId);
                insertFilteredTweet.setString(5, keyword);
                return executeUpdate(insertFilteredTweet);
            } catch (final MysqlDataTruncation e) {
                System.err.println(e.getMessage());
                return DB_ERROR;
            } catch (final SQLException e) {
                e.printStackTrace();
                return DB_ERROR;
            } // catch
        } catch (final SQLException e) {
            e.printStackTrace();
            return DB_ERROR;
        } // catch
    } // insertTweet(long, String, String, String, long, String)

    private void setTweetValues(PreparedStatement s, final long id, final String content,
            final String createdAt, final long userId) throws SQLException {
        s.setLong(1, id);
        s.setString(2, content);
        s.setString(3, createdAt);
        s.setLong(4, userId);
    } // setTweetValues(PreparedStatement, long, String, String, long)

    public int updateSentiment(final String sentiment, final long id) {
        try {
            updateSentiment.setString(1, sentiment);
            updateSentiment.setLong(2, id);
            return executeUpdate(updateSentiment);
        } catch (final SQLException e) {
            e.printStackTrace();
            return DB_ERROR;
        } // catch
    } // updateSentiment(String, long)

    public int updateSentimentScore(final String sentiment, final String sentimentScore,
            final long id) {
        try {
            updateSentimentScore.setString(1, sentiment);
            updateSentimentScore.setDouble(2, Double.parseDouble(sentimentScore));
            updateSentimentScore.setLong(3, id);
            return executeUpdate(updateSentimentScore);
        } catch (final SQLException e) {
            e.printStackTrace();
            return DB_ERROR;
        } // catch
    } // updateSentiment(String, String, long)

    public int insertTweet(final long id, final String screenName, final String content,
            final String createdAt, final long userId) {
        try {
            insertUser(userId, screenName);
            try {
                setTweetValues(insertTweet, id, content, createdAt, userId);
                return executeUpdate(insertTweet);
            } catch (final MysqlDataTruncation e) {
                System.err.println(e.getMessage());
                return DB_ERROR;
            } catch (final SQLException e) {
                e.printStackTrace();
                return DB_ERROR;
            } // catch
        } catch (final SQLException e) {
            e.printStackTrace();
            return DB_ERROR;
        } // catch
    } // insertTweet(long, String, String, String, long)

    public int executeUpdate(final String sqlStatement) {
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

    public ResultSet executeQuery(final String sqlStatement) throws SQLException {
        return con.createStatement().executeQuery(sqlStatement);
    } // executeQuery(String)

    public int deleteAll() {
        final int i = executeUpdate("DELETE FROM tweet");
        final int j = executeUpdate("DELETE FROM user");
        return (i == DB_ERROR || j == DB_ERROR) ? DB_ERROR : i + j;
    } // deleteAll()

    public int deleteTweet(final long tweetId) {
        try {
            deleteTweetByTweetId.setLong(1, tweetId);
            return executeUpdate(deleteTweetByTweetId) * -1;
        } catch (final SQLException e) {
            e.printStackTrace();
            return DB_ERROR;
        } // catch
    } // deleteTweet(long)

    public void close() {
        if (con != null) {
            try {
                con.close();
            } catch (final SQLException e) {
                e.printStackTrace();
            } // catch
        } // if
        mySql = null;
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