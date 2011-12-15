package uk.ac.manchester.cs.patelt9.twitter;

import java.sql.SQLException;

public class InsertCompanyFromFileDictionary extends InsertSoftwareFromFileDictionary {
    public static void main(final String[] args) {
        try {
            new InsertCompanyFromFileDictionary().execute();
        } catch (final SQLException e) {
            e.printStackTrace();
        } // catch
    } // main(String[])

    private InsertCompanyFromFileDictionary() throws SQLException {
        this("dictionary_company.txt");
    } // InsertKeywordFromFileDictionary()

    private InsertCompanyFromFileDictionary(final String path) throws SQLException {
        super(path);
    } // InsertKeywordFromFileDictionary(String)

    @Override
    protected void performTask(final String s) {
        getDb().insertCompany(s);
    } // performTask(String)
} // InsertCompanyFromFileDictionary