package uk.ac.manchester.cs.patelt9.twitter.data;

import java.net.UnknownHostException;

import uk.ac.manchester.cs.patelt9.twitter.data.db.TweetMongoConnector;

import com.mongodb.MongoException;

/**
 * Create a new Thread to carry out all MongoDB tasks in a queue
 *
 * @see DatabaseThread
 */
public final class MongoThread extends DatabaseThread {
    /**
     * Class constructor
     *
     * @throws UnknownHostException
     * @throws MongoException
     */
    public MongoThread() throws UnknownHostException, MongoException {
        this("MONGO");
    } // MongoThread()

    /**
     * Class constructor taking the Thread's name as a parameter.
     * <p>
     * Passes an instance of MongoConnector to its superclass
     *
     * @param s
     *            Thread name
     *
     * @throws UnknownHostException
     * @throws MongoException
     * @see TweetMongoConnector
     */
    public MongoThread(final String s) throws UnknownHostException, MongoException {
        super(s, TweetMongoConnector.getInstance());
    } // MongoThread(String)
} // MongoThread
