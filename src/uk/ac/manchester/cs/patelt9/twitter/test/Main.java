package uk.ac.manchester.cs.patelt9.twitter.test;

public class Main {
    public static void main(final String[] args){
        SqlConnector sql = SqlConnector.getInstance();
        System.out.println(sql.insertUser(251464096));
        sql.close();
    } // main(String[])
} // Main
