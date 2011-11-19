package uk.ac.manchester.cs.patelt9.twitter.data;

import java.net.UnknownHostException;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class MongoConnector {
    private static final String DB_NAME = "TwitterMining";
    public static final int DB_ERROR = -1;

    private static MongoConnector mongoConnector = null;

    private final Mongo mongo;
    private final DB db;
    private final DBCollection userCollection, tweetCollection;

    // Singleton lock on database helper
    public static synchronized MongoConnector getInstance() throws UnknownHostException,
            MongoException {
        if (mongoConnector == null) {
            mongoConnector = new MongoConnector();
        } // if
        return mongoConnector;
    } // getInstance()

    private MongoConnector() throws UnknownHostException, MongoException {
        mongo = new Mongo(/* ip, port */);
        db = mongo.getDB(DB_NAME);
        userCollection = db.getCollection("user");
        tweetCollection = db.getCollection("tweet");
    } // MongoConnector()

    public static void main(String[] args) {
        try {
            getInstance();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (MongoException e) {
            e.printStackTrace();
        } // catch
    } // main(String[])

    public DBObject insertUser(final long id, final String name) {
        final BasicDBObject user = new BasicDBObject();
        user.put("user_id", id);
        user.put("username", name);
        userCollection.insert(user);
        return user;
    } // insertUser(long, String)

    public int insertTweet(final Tweet t) {
        final long userId = t.getUserId();
        final String username = t.getScreenName();
        DBObject user = userCollection.findOne(new BasicDBObject("username", username));
        if (user == null) {
            user = insertUser(userId, username);
        } // if
        final BasicDBObject tweet = new BasicDBObject();
        tweet.put("tweet_id", t.getId());
        tweet.put("user", user);
        tweet.put("text", t.getTweet());
        tweet.put("created_at", t.getCreatedAt());
        return tweetCollection.insert(tweet).getN();
    } // insertTweet(Tweet)

    public int deleteTweet(final long id) {
        return tweetCollection.remove(new BasicDBObject("tweet_id", id)).getN();
    } // deleteTweet(long)

    public int updateSentiment(final long id, final String sentiment, final String score) {
        final BasicDBObject sentimentObject = new BasicDBObject("sentiment", sentiment);
        if (score != null) {
            sentimentObject.put("sentiment_score", score);
        } // if
        tweetCollection.findAndModify(new BasicDBObject("tweet_id", id), sentimentObject);
        return 1;
    } // updateSentiment(long, String, String)

    public long deleteAll() {
        final long count = userCollection.count() + tweetCollection.count();
        userCollection.drop();
        tweetCollection.drop();
        return count;
    } // deleteAll()

    public void close() {
        mongo.close();
        mongoConnector = null;
    } // close()

} // MongoConnector