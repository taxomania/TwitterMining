package uk.ac.manchester.cs.patelt9.twitter.data.db.dictionary;

import java.sql.SQLException;

public class InsertOSFromFileDictionary extends DictionaryFromFile {
    private static final String FILENAME = "operating_systems.txt";
    public static void main(final String[] args) {
        try {
            new InsertOSFromFileDictionary().execute();
        } catch (final SQLException e) {
            e.printStackTrace();
        } // catch
    } // main(String[])

    private InsertOSFromFileDictionary() throws SQLException {
        super(FILENAME);
    } // InsertSoftwareFromFileDictionary()

    @Override
    protected void insert(final String s) {
        db.insertOS(s);
    } // insert(String)
} // InsertOSFromFileDictionary
