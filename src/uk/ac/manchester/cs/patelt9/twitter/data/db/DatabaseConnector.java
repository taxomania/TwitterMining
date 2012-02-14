package uk.ac.manchester.cs.patelt9.twitter.data.db;

/**
 * All database connectors must implement this interface.
 * Allows high level abstraction of database tasks.
 *
 * @author Tariq Patel
 *
 */
public interface DatabaseConnector {
    /**
     * Close all database connections
     */
    void close();

    /**
     * Delete all rows in the database
     *
     * @return Number of rows affected
     */
    int deleteAll();
} // DatabaseConnector
