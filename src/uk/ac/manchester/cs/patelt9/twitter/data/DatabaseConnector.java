package uk.ac.manchester.cs.patelt9.twitter.data;


public interface DatabaseConnector {
    int insertTweet(Tweet t);
    int insertUser(long id, String username) throws Exception;
    int updateSentiment(final long id, final String sentiment);
    int updateSentiment(final long id, final String sentiment, final String score);
    int deleteTweet(long id);
    void close();
    int deleteAll();
} // DatabaseConnector
