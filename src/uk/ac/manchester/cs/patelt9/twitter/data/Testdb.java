package uk.ac.manchester.cs.patelt9.twitter.data;

import java.sql.SQLException;

public class Testdb {
    public static void main(String[] args) throws SQLException {
        DictionarySQLConnector a = DictionarySQLConnector.getInstance();
        a.close();
        TweetSQLConnector c = TweetSQLConnector.getInstance();
        c.close();
        //DictionarySQLConnector a = DictionarySQLConnector.getInstance();
    }
}
