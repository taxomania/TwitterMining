package uk.ac.manchester.cs.patelt9.twitter;

public interface ParseListener {
    void onParseComplete(Tweet t);
    void onParseComplete(long id);
} // ParseListener
