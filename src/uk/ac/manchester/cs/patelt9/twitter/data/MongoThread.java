package uk.ac.manchester.cs.patelt9.twitter.data;

import java.net.UnknownHostException;

import com.mongodb.MongoException;

/**
 * Create a new Thread to carry out all MongoDB tasks in a queue
 *
 * @see DatabaseThread
 */
public final class MongoThread extends DatabaseThread {
    public MongoThread() throws UnknownHostException, MongoException {
        this("Mongo");
    } // MongoThread()

    public MongoThread(final String s) throws UnknownHostException, MongoException {
        super(s, MongoConnector.getInstance());
    } // MongoThread(String)
} // MongoThread