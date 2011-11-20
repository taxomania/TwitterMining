package uk.ac.manchester.cs.patelt9.twitter.data.task;

import uk.ac.manchester.cs.patelt9.twitter.data.Tweet;

public class InsertKeywordTask extends InsertTask {
    public InsertKeywordTask(final Tweet t, final String filter) {
        super(t);
        t.setKeyword(filter);
    } // InsertKeywordTask(Tweet, String)
} // InsertKeywordTask
