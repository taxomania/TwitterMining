package uk.ac.manchester.cs.patelt9.twitter.practice;

import java.util.ArrayList;
import java.util.List;

// This class is used to link tweets to users before adding to a database
public class TwitterUser {
    private final long id;
    private final List<Tweet> tweets;

    public TwitterUser(final long id) {
        this.id = id;
        tweets = new ArrayList<Tweet>();
    } // TwitterUser(long)

    public boolean addTweet(final Tweet tweet) {
        return tweets.add(tweet);
    } // addTweet(String)

    public long getId(){
        return id;
    } // getId()

    public List<Tweet> getTweets(){
        return tweets;
    } // getTweets()

    public String toString(){
        return Long.toString(id) + ": " + tweets.toString();
    } // toString()
} // TwitterUser
