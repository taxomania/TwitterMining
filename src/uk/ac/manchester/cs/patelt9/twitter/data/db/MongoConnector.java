package uk.ac.manchester.cs.patelt9.twitter.data.db;

import java.net.UnknownHostException;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

/**
 * Helper class to connect to MongoDB and carry out database operations.
 *
 * @author Tariq Patel
 *
 */
public abstract class MongoConnector implements DatabaseConnector {
    private static final String DB_NAME = "TwitterMining";

    private static Mongo mongo = null;
    private DB db;

    protected MongoConnector() throws UnknownHostException, MongoException {
        if (mongo == null) {
            mongo = new Mongo(/* ip, port */);
        } // if
        db = mongo.getDB(DB_NAME);
    } // MongoConnector()

    public final DB getDb() {
        return db;
    } // getDb()

    @Override
    public void close() {
        if (mongo != null) {
            mongo.close();
            mongo = null;
        } // if
    } // close()
} // MongoConnector
