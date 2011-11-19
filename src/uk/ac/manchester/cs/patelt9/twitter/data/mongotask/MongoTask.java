package uk.ac.manchester.cs.patelt9.twitter.data.mongotask;

import uk.ac.manchester.cs.patelt9.twitter.data.MongoConnector;

public interface MongoTask {
    public int doMongoTask(MongoConnector mongo);
    public String toString();
} // MongoTask