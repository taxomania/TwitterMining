package uk.ac.manchester.cs.patelt9.twitter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;

import uk.ac.manchester.cs.patelt9.twitter.data.db.DictionarySQLConnector;

public class InsertSoftwareFromFileDictionary {
    public static void main(final String[] args) {
        try {
            new InsertSoftwareFromFileDictionary().execute();
        } catch (final SQLException e) {
            e.printStackTrace();
        } // catch
    } // main(String[])

    final String filepath;
    final DictionarySQLConnector db;

    private InsertSoftwareFromFileDictionary() throws SQLException {
        this("dictionary.txt");
    } // InsertSoftwareFromFileDictionary()

    protected InsertSoftwareFromFileDictionary(final String path) throws SQLException {
        filepath = path;
        db = DictionarySQLConnector.getInstance();
    } // InsertSoftwareFromFileDictionary(String)

    protected void performTask(final String s) {
        db.insert(s);
    } // performTask(String)

    public DictionarySQLConnector getDb() {
        return db;
    } // getDb()

    protected void execute() {
        BufferedReader r = null;
        try {
            r = new BufferedReader(new FileReader(new File(filepath)));
            String s;
            while ((s = r.readLine()) != null) {
                performTask(s);
            } // while
            db.close();
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
            System.err.println("Login file not found");
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            if (r != null) {
                try {
                    r.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                } // catch
            } // if
        } // finally
    } // execute()
} // InsertSoftwareFromFileDictionary
