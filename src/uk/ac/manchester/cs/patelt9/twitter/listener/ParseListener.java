package uk.ac.manchester.cs.patelt9.twitter.listener;

import uk.ac.manchester.cs.patelt9.twitter.Tweet;

public interface ParseListener {
    void onParseComplete(Tweet t);
    void onParseComplete(long id);
} // ParseListener
