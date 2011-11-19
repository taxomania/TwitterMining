package uk.ac.manchester.cs.patelt9.twitter.data.mongotask;

import uk.ac.manchester.cs.patelt9.twitter.data.MongoConnector;

public class DeleteTweetMongoTask implements MongoTask {
    private final long id;

    public DeleteTweetMongoTask(final long id) {
        this.id = id;
    } // DeleteTweetIdSQLTask(long)

    @Override
    public int doMongoTask(final MongoConnector mongo) {
        return mongo.deleteTweet(id);
    } // doSqlTask(SqlConnector)

    @Override
    public String toString() {
        return Long.toString(id);
    } // toString()

} // DeleteTweetMongoTask