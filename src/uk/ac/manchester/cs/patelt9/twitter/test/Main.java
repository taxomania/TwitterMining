package uk.ac.manchester.cs.patelt9.twitter.test;

public class Main {
    public static void main(final String[] args){
        long id = 251464096;
        SqlConnector sql = SqlConnector.getInstance();
      //  System.out.println(sql.insertUser(id));
        String content = "Hello world";
        String createdAt = "2011-10-26 14:08:37";
        System.out.println(sql.insertTweet(id, content, createdAt));
        sql.close();
    } // main(String[])
} // Main
