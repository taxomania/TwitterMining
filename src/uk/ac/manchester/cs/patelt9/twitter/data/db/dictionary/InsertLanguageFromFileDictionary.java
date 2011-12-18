package uk.ac.manchester.cs.patelt9.twitter.data.db.dictionary;

import java.sql.SQLException;

public class InsertLanguageFromFileDictionary extends DictionaryFromFile {
    private static final String FILENAME = "programming_languages.txt";
    public static void main(final String[] args) {
        try {
            new InsertLanguageFromFileDictionary().execute();
        } catch (final SQLException e) {
            e.printStackTrace();
        } // catch
    } // main(String[])

    private InsertLanguageFromFileDictionary() throws SQLException {
        super(FILENAME);
    } // InsertLanguageFromFileDictionary()

    @Override
    protected void insert(final String s) {
        db.insertLanguage(s);
    } // insert(String)
} // InsertLanguageFromFileDictionary
