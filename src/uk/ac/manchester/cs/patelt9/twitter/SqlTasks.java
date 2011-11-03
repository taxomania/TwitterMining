package uk.ac.manchester.cs.patelt9.twitter;

import java.sql.SQLException;

import uk.ac.manchester.cs.patelt9.twitter.data.SqlConnector;

public class SqlTasks {
    public static void main(final String[] args) {
        final SqlConnector sql;
        try {
            sql = SqlConnector.getInstance();
            delete(args, sql);
        } catch (final SQLException e) {
            System.err.println("Failed to connect to database");
            return;
        } // catch
        sql.close();
    } // main(String[])

    public static void delete(final String[] args, final SqlConnector sql) {
        if (args != null && args.length != 0) {
            if (args[0].equals("delete")) {
                System.out.println(sql.deleteAll() + " rows deleted");
            } // if
        } else {
            System.out.println(sql.deleteError() + " tweets deleted");
        } // else
    } // delete(args)
} // SqlTasks