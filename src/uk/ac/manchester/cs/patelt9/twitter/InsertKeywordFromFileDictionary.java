package uk.ac.manchester.cs.patelt9.twitter;

import java.sql.SQLException;

public class InsertKeywordFromFileDictionary extends InsertSoftwareFromFileDictionary {
    public static void main(final String[] args) {
        try {
            new InsertKeywordFromFileDictionary().execute();
        } catch (final SQLException e) {
            e.printStackTrace();
        } // catch
    } // main(String[])

    private InsertKeywordFromFileDictionary() throws SQLException {
        this("keywords.txt");
    } // InsertKeywordFromFileDictionary()

    private InsertKeywordFromFileDictionary(final String path) throws SQLException {
        super(path);
    } // InsertKeywordFromFileDictionary(String)

    @Override
    protected void performTask(final String s) {
        getDb().insertKeyword(s);
    } // performTask(String)
} // InsertKeywordFromFileDictionary