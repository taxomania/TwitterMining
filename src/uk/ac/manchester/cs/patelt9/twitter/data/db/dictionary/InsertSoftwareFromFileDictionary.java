package uk.ac.manchester.cs.patelt9.twitter.data.db.dictionary;

import java.sql.SQLException;

public class InsertSoftwareFromFileDictionary extends DictionaryFromFile {
    private static final String FILENAME = "dictionary.txt";
    public static void main(final String[] args) {
        try {
            new InsertSoftwareFromFileDictionary().execute();
        } catch (final SQLException e) {
            e.printStackTrace();
        } // catch
    } // main(String[])

    private InsertSoftwareFromFileDictionary() throws SQLException {
        super(FILENAME);
    } // InsertSoftwareFromFileDictionary()

    @Override
    protected void insert(final String s) {
        db.insert(s.split("\t"));
    } // insert(String)
} // InsertSoftwareFromFileDictionary
