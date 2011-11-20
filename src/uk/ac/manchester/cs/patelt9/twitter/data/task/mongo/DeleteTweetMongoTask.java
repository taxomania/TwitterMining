package uk.ac.manchester.cs.patelt9.twitter.data.task.mongo;

import uk.ac.manchester.cs.patelt9.twitter.data.MongoConnector;
import uk.ac.manchester.cs.patelt9.twitter.data.task.DeleteTask;

public class DeleteTweetMongoTask extends DeleteTask implements MongoTask {
    public DeleteTweetMongoTask(final long id) {
        super(id);
    } // DeleteTweetIdSQLTask(long)

    @Override
    public int doMongoTask(final MongoConnector mongo) {
        return mongo.deleteTweet(getId());
    } // doSqlTask(SqlConnector)

} // DeleteTweetMongoTask