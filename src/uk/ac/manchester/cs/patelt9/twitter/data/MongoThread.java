package uk.ac.manchester.cs.patelt9.twitter.data;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import uk.ac.manchester.cs.patelt9.twitter.data.mongotask.MongoTask;
import uk.ac.manchester.cs.patelt9.twitter.data.task.DatabaseTask;

import com.mongodb.MongoException;

public class MongoThread extends DatabaseThread {
    private final MongoConnector mongo;
    private final List<MongoTask> taskList = new ArrayList<MongoTask>();
    private int affectedRows;

    public MongoThread() throws UnknownHostException, MongoException {
        this("Mongo");
    } // MongoThread()

    public MongoThread(final String s) throws UnknownHostException, MongoException {
        super(s);
        mongo = MongoConnector.getInstance();
        affectedRows = 0;
    } // MongoThread(String)

    @Override
    public final void run() {
        while (!isInterrupted()) {
            try {
                synchronized (taskList) {
                    performTask();
                } // synchronized
            } catch (final IndexOutOfBoundsException e) {
                continue;
            } // catch
        } // while
    } // run()

    @Override
    public void interrupt() {
        synchronized (taskList) {
            while (!taskList.isEmpty()) {
                performTask();
            } // while
        } // synchronized
        mongo.close();
        System.out.println(affectedRows + " rows affected");
        super.interrupt();
    } // interrupt()

    @Override
    protected void performTask() {
        affectedRows += taskList.remove(0).doMongoTask(mongo);
    } // performTask()

    @Override
    public boolean addTask(final DatabaseTask task) {
        synchronized (taskList) {
            return taskList.add((MongoTask) task);
        } // synchronized
    } // addTask(MongoTask)
} // MongoThread