package uk.ac.manchester.cs.patelt9.twitter.data.task;

import uk.ac.manchester.cs.patelt9.twitter.data.DatabaseConnector;

public abstract class DeleteTask implements DatabaseTask {
    private final long id;

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
    public int doTask(final DatabaseConnector db) {
        return db.deleteTweet(id);
    } // doTask(DatabaseConnector)
} // DeleteTask
