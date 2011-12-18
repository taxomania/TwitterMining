package uk.ac.manchester.cs.patelt9.twitter.data.db.dictionary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;

import uk.ac.manchester.cs.patelt9.twitter.data.db.DictionarySQLConnector;

public abstract class DictionaryFromFile {
    private static final String DIRECTORY = "dictionary/";
    private final String filename;
    protected final DictionarySQLConnector db;

    protected DictionaryFromFile(final String path) throws SQLException {
        filename = path;
        db = DictionarySQLConnector.getInstance();
    } // DictionaryFromFile(String)

    protected abstract void insert(final String s);

    protected void execute() {
        BufferedReader r = null;
        try {
            r = new BufferedReader(new FileReader(new File(DIRECTORY + filename)));
            String s;
            while ((s = r.readLine()) != null) {
                insert(s);
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
} // DictionaryFromFile
