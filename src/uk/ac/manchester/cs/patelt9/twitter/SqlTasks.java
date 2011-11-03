package uk.ac.manchester.cs.patelt9.twitter;

import java.sql.SQLException;

import uk.ac.manchester.cs.patelt9.twitter.data.SqlConnector;

public class SqlTasks {
    public static void main(final String[] args) {
        final SqlConnector sql;
        try {
            sql = SqlConnector.getInstance();
        } catch (final SQLException e) {
            System.err.println("Failed to connect to database");
            return;
        } // catch
        if (args.length != 0) {
            if (args[0].equals("delete")) {
                System.out.println(sql.deleteAll() + " rows deleted");
            } // if
        } else {
            System.out.println(sql.deleteError() + " tweets deleted");
        } // else
        sql.close();
    } // main(String[])
} // SqlTasks