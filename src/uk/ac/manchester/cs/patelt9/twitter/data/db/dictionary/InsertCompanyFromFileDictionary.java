package uk.ac.manchester.cs.patelt9.twitter.data.db.dictionary;

import java.sql.SQLException;

public class InsertCompanyFromFileDictionary extends DictionaryFromFile {
    public static void main(final String[] args) {
        try {
            new InsertCompanyFromFileDictionary().execute();
        } catch (final SQLException e) {
            e.printStackTrace();
        } // catch
    } // main(String[])

    private InsertCompanyFromFileDictionary() throws SQLException {
        super("dictionary_company.txt");
    } // InsertKeywordFromFileDictionary()

    @Override
    protected void insert(final String s) {
        db.insertCompany(s);
    } // insert(String)
} // InsertCompanyFromFileDictionary