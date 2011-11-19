package uk.ac.manchester.cs.patelt9.twitter.data.mongotask;

import uk.ac.manchester.cs.patelt9.twitter.data.MongoConnector;
import uk.ac.manchester.cs.patelt9.twitter.data.task.DatabaseTask;

public interface MongoTask extends DatabaseTask {
    public int doMongoTask(MongoConnector mongo);
} // MongoTask