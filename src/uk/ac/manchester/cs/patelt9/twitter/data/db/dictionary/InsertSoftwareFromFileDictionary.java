package uk.ac.manchester.cs.patelt9.twitter.data.db.dictionary;

import java.sql.SQLException;

public class InsertSoftwareFromFileDictionary extends DictionaryFromFile {
    public static void main(final String[] args) {
        try {
            new InsertSoftwareFromFileDictionary().execute();
        } catch (final SQLException e) {
            e.printStackTrace();
        } // catch
    } // main(String[])

    private InsertSoftwareFromFileDictionary() throws SQLException {
        super("dictionary.txt");
    } // InsertSoftwareFromFileDictionary()

    @Override
    protected void insert(final String s) {
        db.insert(s.split("\t"));
    } // insert(String)
} // InsertSoftwareFromFileDictionary
