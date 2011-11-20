package uk.ac.manchester.cs.patelt9.twitter.data.task;

import uk.ac.manchester.cs.patelt9.twitter.data.Tweet;

/**
 * DatabaseTask allowing database insertion with a keyword.
 *
 * @author Tariq Patel
 *
 */
public class InsertKeywordTask extends InsertTask {
    /**
     * Constructor adds the keyword to the Tweet object.
     *
     * @param t
     *            Tweet to insert and update.
     * @param filter
     *            The keywords used to retrieve the given tweet from Twitter.
     */
    public InsertKeywordTask(final Tweet t, final String filter) {
        super(t);
        t.setKeyword(filter);
    } // InsertKeywordTask(Tweet, String)
} // InsertKeywordTask
