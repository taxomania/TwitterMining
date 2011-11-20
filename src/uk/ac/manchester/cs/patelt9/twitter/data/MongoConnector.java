package uk.ac.manchester.cs.patelt9.twitter.data;

import java.net.UnknownHostException;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public final class MongoConnector implements DatabaseConnector {
    private static final String DB_NAME = "TwitterMining";
    public static final int DB_ERROR = -1;

    private static MongoConnector mongoConnector = null;

    private final Mongo mongo;
    private final DB db;
    private final DBCollection userCollection, tweetCollection;

    /**
     * Retrieve the current instance of MongoConnector, or create a new one if it is null;
     *
     * @return A single instance of MongoConnector
     * @throws UnknownHostException
     * @throws MongoException
     */
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
            MongoConnector m = getInstance();
            m.insertTweet(new Tweet(14142411, "HELLO WORLD", "1411=6-16", new User(141455,
                    "taxomania")));
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

    public DBObject insertNewUser(final User u) {
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
        final BasicDBObject tweet = new BasicDBObject();
        tweet.put("tweet_id", t.getId());
        tweet.put("user", user);
        tweet.put("text", t.getTweet());
        tweet.put("created_at", t.getCreatedAt());
        if (tweetCollection.findOne(tweet) == null) {
            System.out.println("Insert");
            return tweetCollection.insert(tweet).getN();
        } else {
            System.out.println("Exists");
            return 0;
        } // else
    } // insertTweet(Tweet)

    public int deleteTweet(final long id) {
        return tweetCollection.remove(new BasicDBObject("tweet_id", id)).getN();
    } // deleteTweet(long)

    // public void addEntities(final long id, final Entity[] entities){
    // tweetCollection.find
    // }

    public int updateSentiment(final long id, final String sentiment) {
        return updateSentiment(id, sentiment, null);
    } // updateSentiment(long, String)

    public int updateSentiment(final long id, final String sentiment, final String score) {
        final BasicDBObject sentimentObject = new BasicDBObject("sentiment", sentiment);
        if (score != null) {
            sentimentObject.put("sentiment_score", score);
        } // if
        tweetCollection.findAndModify(new BasicDBObject("tweet_id", id), sentimentObject);
        return 1;
    } // updateSentiment(long, String, String)

    public int deleteAll() {
        final long count = userCollection.count() + tweetCollection.count();
        userCollection.drop();
        tweetCollection.drop();
        return (int) count;
    } // deleteAll()

    public void close() {
        mongo.close();
        mongoConnector = null;
    } // close()

} // MongoConnector