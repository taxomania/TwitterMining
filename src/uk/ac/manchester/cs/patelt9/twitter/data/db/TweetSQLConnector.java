package uk.ac.manchester.cs.patelt9.twitter.data.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import uk.ac.manchester.cs.patelt9.twitter.data.Tweet;
import uk.ac.manchester.cs.patelt9.twitter.data.User;

import com.mysql.jdbc.MysqlDataTruncation;

/**
 * Helper class to connect to MySQL and carry out database operations.
 *
 * @author Tariq Patel
 *
 */
public final class TweetSQLConnector extends SQLConnector implements DatabaseConnector {
    private PreparedStatement insertUser = null;
    private PreparedStatement insertFilteredTweet = null;
    private PreparedStatement insertTweet = null;
    private PreparedStatement deleteTweetByTweetId = null;
    private PreparedStatement updateSentiment = null;
    private PreparedStatement updateSentimentScore = null;

    private final Connection con = getConnection();
    private static TweetSQLConnector mySql = null;

    /**
     * Retrieve the current instance of SQLConnector, or create a new one if it is null;
     *
     * @return A single instance of SQLConnector
     * @throws SQLException
     */
    public static synchronized TweetSQLConnector getInstance() throws SQLException {
        if (mySql == null) {
            mySql = new TweetSQLConnector();
        } // if
        return mySql;
    } // getInstance()

    private TweetSQLConnector() throws SQLException {
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
                "?, " + // Keyword
                "default);"); // tagged

        insertTweet = con.prepareStatement(
                "INSERT INTO tweet VALUES(default, " + // Id
                "?, " + // Tweet_Id
                "?, " + // Text
                "?, " + // Created_at
                "?, " + // User_Id
                "default, default, default, default);"); // Sentiment, Sentiment_score, Keyword, tagged

        deleteTweetByTweetId = con.prepareStatement("DELETE FROM tweet WHERE tweet_id=?;");

        updateSentiment = con.prepareStatement("UPDATE tweet SET sentiment=? WHERE tweet_id=?;");
        updateSentimentScore = con.prepareStatement("UPDATE tweet SET sentiment=?, " +
                "sentiment_score=? WHERE tweet_id=?;");
        // @formatter:on
    } // SQLConnector()

    @Override
    public int insertUser(final User user) throws SQLException {
        return insertUser(user.getId(), user.getUsername());
    } // insertUser(User)

    private int insertUser(final long id, final String screenName) throws SQLException {
        insertUser.setLong(1, id);
        insertUser.setString(2, screenName);
        return executeUpdate(insertUser);
    } // insertUser(long, String)

    @Override
    public int insertTweet(final Tweet t) {
        final String keyword = t.getKeyword();
        if (keyword == null) {
            return insertTweet(t.getId(), t.getScreenName(), t.getTweet(), t.getCreatedAt(),
                    t.getUserId());
        } else {
            return insertTweet(t.getId(), t.getScreenName(), t.getTweet(), t.getCreatedAt(),
                    t.getUserId(), keyword);
        } // else
    } // insertTweet(Tweet)

    private int insertTweet(final long id, final String screenName, final String content,
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

    private int insertTweet(final long id, final String screenName, final String content,
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

    private void setTweetValues(PreparedStatement s, final long id, final String content,
            final String createdAt, final long userId) throws SQLException {
        s.setLong(1, id);
        s.setString(2, content);
        s.setString(3, createdAt);
        s.setLong(4, userId);
    } // setTweetValues(PreparedStatement, long, String, String, long)

    @Override
    public int updateSentiment(final long id, final String sentiment) {
        try {
            updateSentiment.setString(1, sentiment);
            updateSentiment.setLong(2, id);
            return executeUpdate(updateSentiment);
        } catch (final SQLException e) {
            e.printStackTrace();
            return DB_ERROR;
        } // catch
    } // updateSentiment(long, String)

    @Override
    public int updateSentiment(final long id, final String sentiment, final String sentimentScore) {
        try {
            updateSentimentScore.setString(1, sentiment);
            updateSentimentScore.setDouble(2, Double.parseDouble(sentimentScore));
            updateSentimentScore.setLong(3, id);
            return executeUpdate(updateSentimentScore);
        } catch (final SQLException e) {
            e.printStackTrace();
            return DB_ERROR;
        } // catch
    } // updateSentiment(long, String, String)

    /**
     * Execute any MySQL query.
     *
     * @param sqlStatement
     *            The query to execute, typically a SELECT statement
     * @return The ResultSet object that contains the data produced by the given query, never null
     * @throws SQLException
     */
    public ResultSet executeQuery(final String sqlStatement) throws SQLException {
        return con.createStatement().executeQuery(sqlStatement);
    } // executeQuery(String)

    @Override
    public int deleteAll() {
        final int i = executeUpdate("TRUNCATE TABLE tweet");
        final int j = executeUpdate("TRUNCATE TABLE user");
        return (i == DB_ERROR || j == DB_ERROR) ? DB_ERROR : i + j;
    } // deleteAll()

    @Override
    public int deleteTweet(final long tweetId) {
        try {
            deleteTweetByTweetId.setLong(1, tweetId);
            return executeUpdate(deleteTweetByTweetId) * -1;
        } catch (final SQLException e) {
            e.printStackTrace();
            return DB_ERROR;
        } // catch
    } // deleteTweet(long)

    @Override
    public void close() {
        super.close();
        mySql = null;
    } // close()
} // SQLConnector
