package uk.ac.manchester.cs.patelt9.twitter.data.db;

import java.net.UnknownHostException;

import uk.ac.manchester.cs.patelt9.twitter.data.Tweet;
import uk.ac.manchester.cs.patelt9.twitter.data.User;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

/**
 * Helper class to connect to MongoDB and carry out database operations.
 *
 * @author Tariq Patel
 *
 */
public final class TweetMongoConnector extends MongoConnector implements TweetDatabaseConnector {
    private static TweetMongoConnector mongoConnector = null;

    private final DBCollection userCollection, tweetCollection;

    /**
     * Retrieve the current instance of MongoConnector, or create a new one if it is null;
     *
     * @return A single instance of MongoConnector
     * @throws UnknownHostException
     * @throws MongoException
     */
    public static synchronized TweetMongoConnector getInstance() throws UnknownHostException,
            MongoException {
        if (mongoConnector == null) {
            mongoConnector = new TweetMongoConnector();
        } // if
        return mongoConnector;
    } // getInstance()

    private TweetMongoConnector() throws UnknownHostException, MongoException {
        super();
        final DB db = getDb();
        userCollection = db.getCollection("user");
        tweetCollection = db.getCollection("tweet");
    } // MongoConnector()

    public static void main(String[] args) {
        try {
            TweetMongoConnector m = getInstance();
            // m.deleteAll();
            // m.insertTweet(new Tweet(14142411, "HELLO WORLD", "1411=6-16", new User(141455,
            // "taxomania")));
            // m.insertTweet(new Tweet(23142411, "THAT WAS SO AWESOME!", "1411=6-16", new
            // User(141455,
            // "taxomania")));
            m.close();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (MongoException e) {
            e.printStackTrace();
        } // catch
    } // main(String[])

    @Override
    public int insertUser(final User user) {
        return (insertNewUser(user) != null) ? 1 : 0;
    } // insertUser(long, String)

    private DBObject insertNewUser(final User u) {
        final BasicDBObject user = new BasicDBObject();
        user.put("user_id", u.getId());
        user.put("username", u.getUsername());
        userCollection.insert(user);
        return user;
    } // insertNewUser(long, String)

    @Override
    public int insertTweet(final Tweet t) {
        DBObject user = userCollection.findOne(new BasicDBObject("user_id", t.getUserId()));
        if (user == null) {
            user = insertNewUser(t.getUser());
        } // if
        final BasicDBObject tweet = new BasicDBObject("tweet_id", t.getId());
        if (tweetCollection.findOne(tweet) == null) {
            tweet.put("text", t.getTweet());
            tweet.put("created_at", t.getCreatedAt());
            tweet.put("user", user);
            return tweetCollection.insert(tweet).getN();
        } else {
            return 0;
        } // else
    } // insertTweet(Tweet)

    @Override
    public int deleteTweet(final long id) {
        return tweetCollection.remove(new BasicDBObject("tweet_id", id)).getN();
    } // deleteTweet(long)

    @Override
    public int updateSentiment(final long id, final String sentiment) {
        return updateSentiment(id, sentiment, null);
    } // updateSentiment(long, String)

    @Override
    public int updateSentiment(final long id, final String sentiment, final String score) {
        final BasicDBObject sentimentObject = new BasicDBObject("sentiment", sentiment);
        if (score != null) {
            sentimentObject.put("sentiment_score", score);
        } // if
        tweetCollection.setObjectClass(BasicDBObject.class);
        final BasicDBObject tweet = (BasicDBObject) tweetCollection.findOne(new BasicDBObject(
                "tweet_id", id));
        tweet.put("sentiment", sentimentObject);
        return tweetCollection.update(new BasicDBObject("tweet_id", id), tweet).getN();
    } // updateSentiment(long, String, String)

    public DBCursor loadSentimentDataSet() {
        return tweetCollection.find(new BasicDBObject("sentiment", new BasicDBObject("$exists",
                false)));
    } // loadSentimentDataSet()

    @Override
    public int deleteAll() {
        final long count = userCollection.count() + tweetCollection.count();
        userCollection.drop();
        tweetCollection.drop();
        return (int) count;
    } // deleteAll()

    @Override
    public void close() {
        super.close();
        mongoConnector = null;
    } // close()
} // MongoConnector
