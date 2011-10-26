package uk.ac.manchester.cs.patelt9.twitter.practice;

public class Main {
    public static void main(final String[] args){
        JDBCTest sql = JDBCTest.getInstance();
        System.out.println(sql.insertUser(251464096));
        sql.close();
    } // main(String[])
} // Main
