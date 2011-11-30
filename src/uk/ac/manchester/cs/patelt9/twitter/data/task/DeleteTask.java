package uk.ac.manchester.cs.patelt9.twitter.data.task;

import uk.ac.manchester.cs.patelt9.twitter.data.TweetDatabaseConnector;

/**
 * DatabaseTask to allow deleting from the database.
 *
 * @author Tariq Patel
 *
 */
public class DeleteTask implements DatabaseTask {
    private final long id;

    /**
     *
     * @param id
     *            Tweet id
     */
    public DeleteTask(final long id) {
        this.id = id;
    } // DeleteTask(long)

    protected long getId() {
        return id;
    } // getId()

    @Override
    public String toString() {
        return Long.toString(id);
    } // toString()

    @Override
    public int doTask(final TweetDatabaseConnector db) {
        return db.deleteTweet(id);
    } // doTask(DatabaseConnector)
} // DeleteTask
