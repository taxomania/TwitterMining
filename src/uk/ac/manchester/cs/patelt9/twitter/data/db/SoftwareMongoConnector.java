package uk.ac.manchester.cs.patelt9.twitter.data.db;

import java.net.UnknownHostException;
import java.sql.SQLException;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoException;

/**
 * Helper class to connect to MongoDB and carry out database operations.
 *
 * @author Tariq Patel
 *
 */
public final class SoftwareMongoConnector extends MongoConnector {
    private static SoftwareMongoConnector mongoConnector = null;

    private final DBCollection collection;

    /**
     * Retrieve the current instance of MongoConnector, or create a new one if it is null;
     *
     * @return A single instance of MongoConnector
     * @throws UnknownHostException
     * @throws MongoException
     */
    public static synchronized SoftwareMongoConnector getInstance() throws UnknownHostException,
            MongoException {
        if (mongoConnector == null) {
            mongoConnector = new SoftwareMongoConnector();
        } // if
        return mongoConnector;
    } // getInstance()

    private SoftwareMongoConnector() throws UnknownHostException, MongoException {
        super();
        collection = getDb().getCollection("tagged_tweets");
        // System.out.println(collection.getFullName()); // Test
    } // SoftwareMongoConnector()

    public static void main(String[] args) throws MongoException, UnknownHostException {
        getInstance().close();
    }

    public DBCursor selectAll() {
        return collection.find();
    } // selectAll()

    @Override
    public int deleteAll() {
        try {
            final long count = collection.count()
                    + TweetSQLConnector.getInstance().resetTweetTaggedField();
            collection.drop(); // Only drop table if it works
            return (int) count;
        } catch (final SQLException e) {
            e.printStackTrace();
            return 0;
        } // catch
    } // deleteAll()

    @Override
    public void close() {
        super.close();
        mongoConnector = null;
    } // close()
} // SoftwareMongoConnector
