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
        new InsertSoftwareFromFileDictionary().execute();
    } // main(String[])

    final String filepath;

    private InsertSoftwareFromFileDictionary() {
        this("dictionary.txt");
    } // InsertSoftwareFromFileDictionary()

    private InsertSoftwareFromFileDictionary(final String path) {
        filepath = path;
    } // InsertSoftwareFromFileDictionary(String)

    private void execute() {
        BufferedReader r = null;
        try {
            r = new BufferedReader(new FileReader(new File("dictionary.txt")));
            try {
                final DictionarySQLConnector db = DictionarySQLConnector.getInstance();
                String s;
                while ((s = r.readLine()) != null) {
                    db.insert(s);
                } // while
                db.close();
            } catch (final SQLException e) {
                e.printStackTrace();
            } // catch
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
