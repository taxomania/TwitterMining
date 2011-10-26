package uk.ac.manchester.cs.patelt9.twitter.test;

import uk.ac.manchester.cs.patelt9.twitter.SqlConnector;

public class Main {
    public static void main(final String[] args){
        SqlConnector sql = SqlConnector.getInstance();
        if (args.length != 0){
            if (args[0].equals("delete")){
                System.out.println(sql.deleteAll());
                return;
            } // if
        } // if

        //long id = 251464096;
          //System.out.println(sql.insertUser(id));
          //System.out.println(sql.insertUser(id+1));
          //System.out.println(sql.insertUser(id+2));
       // String content = "Hello world";
     //   String createdAt = "2011-10-26 14:08:37";
  //      System.out.println(sql.insertTweet(id, content, createdAt));
    //    System.out.println(sql.insertTweet(id+1, content, createdAt));
      //  System.out.println(sql.insertTweet(id+1, "BELUGH", createdAt));
        //System.out.println(sql.insertTweet(id+2, "BELGH", createdAt));
        sql.close();
    } // main(String[])
} // Main
