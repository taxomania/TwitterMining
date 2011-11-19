package uk.ac.manchester.cs.patelt9.twitter.data.task;

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

} // DeleteTask
