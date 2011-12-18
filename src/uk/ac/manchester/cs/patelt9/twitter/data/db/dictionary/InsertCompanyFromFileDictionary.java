package uk.ac.manchester.cs.patelt9.twitter.data.db.dictionary;

import java.sql.SQLException;

public class InsertCompanyFromFileDictionary extends DictionaryFromFile {
    private static final String FILENAME = "dictionary_company.txt";
    public static void main(final String[] args) {
        try {
            new InsertCompanyFromFileDictionary().execute();
        } catch (final SQLException e) {
            e.printStackTrace();
        } // catch
    } // main(String[])

    private InsertCompanyFromFileDictionary() throws SQLException {
        super(FILENAME);
    } // InsertCompanyFromFileDictionary()

    @Override
    protected void insert(final String s) {
        db.insertCompany(s);
    } // insert(String)
} // InsertCompanyFromFileDictionary
