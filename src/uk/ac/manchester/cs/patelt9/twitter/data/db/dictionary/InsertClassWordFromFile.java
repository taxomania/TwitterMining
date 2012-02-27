package uk.ac.manchester.cs.patelt9.twitter.data.db.dictionary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;

import uk.ac.manchester.cs.patelt9.twitter.data.db.ClassesSQLConnector;

public class InsertClassWordFromFile {
    private static final String FILENAME = "classifier/classes.txt";
    protected final ClassesSQLConnector db;

    private InsertClassWordFromFile() throws SQLException {
        db = ClassesSQLConnector.getInstance();
    } // DictionaryFromFile(String)

    protected void execute() {
        BufferedReader r = null;
        try {
            r = new BufferedReader(new FileReader(new File(FILENAME)));
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

    public static void main(final String[] args) {
        try {
            new InsertClassWordFromFile().execute();
        } catch (final SQLException e) {
            e.printStackTrace();
        } // catch
    } // main(String[])

    private void insert(final String s) {
        db.insert(s.split("\t"));
    } // insert(String)
} // InsertClassFromFile
