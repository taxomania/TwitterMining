package uk.ac.manchester.cs.patelt9.twitter.data.db.dictionary;

import java.sql.SQLException;

public class InsertKeywordFromFileDictionary extends DictionaryFromFile {
    public static void main(final String[] args) {
        try {
            new InsertKeywordFromFileDictionary().execute();
        } catch (final SQLException e) {
            e.printStackTrace();
        } // catch
    } // main(String[])

    private InsertKeywordFromFileDictionary() throws SQLException {
        super("keywords.txt");
    } // InsertKeywordFromFileDictionary()

    @Override
    protected void insert(final String s) {
        db.insertKeyword(s);
    } // insert(String)
} // InsertKeywordFromFileDictionary