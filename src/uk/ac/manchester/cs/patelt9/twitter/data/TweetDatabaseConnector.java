package uk.ac.manchester.cs.patelt9.twitter.data;

/**
 * All database connectors must implement this interface.
 * Allows high level abstraction of database tasks.
 *
 * @author Tariq Patel
 *
 */
public interface TweetDatabaseConnector {
    /**
     * Insert a Tweet object into the database
     *
     * @param t
     *            The Tweet object to insert
     * @return Number of affected rows
     */
    int insertTweet(Tweet t);

    /**
     * Insert a User object into the database
     *
     * @param user
     *            The user object to insert
     * @return Number of affected rows
     * @throws Exception
     */
    int insertUser(User user) throws Exception;

    /**
     * Update a tweet's sentiment where sentiment is neutral
     *
     * @param id
     *            Tweet id
     * @param sentiment
     *            Sentiment result
     * @return Number of rows affected
     */
    int updateSentiment(final long id, final String sentiment);

    /**
     * Update a tweet's sentiment where sentiment is positive or negative and has a score
     *
     * @param id
     *            Tweet id
     * @param sentiment
     *            Sentiment result
     * @param score
     *            Certainty of given sentiment result
     * @return Number of rows affected
     */
    int updateSentiment(final long id, final String sentiment, final String score);

    /**
     * Delete a given tweet by its id
     *
     * @param id
     *            Tweet id
     * @return Number of rows affected
     */
    int deleteTweet(long id);

    /**
     * Close all database connections
     */
    void close();

    /**
     * Delete all rows in the database
     *
     * @return Number of rows affected
     */
    int deleteAll();
} // DatabaseConnector
