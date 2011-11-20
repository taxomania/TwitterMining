package uk.ac.manchester.cs.patelt9.twitter.data.task;

import uk.ac.manchester.cs.patelt9.twitter.data.DatabaseConnector;
import uk.ac.manchester.cs.patelt9.twitter.data.SqlConnector;
import uk.ac.manchester.cs.patelt9.twitter.data.Tweet;

public class InsertKeywordSQLTask extends InsertTask {
    private final String keyword;

    public InsertKeywordSQLTask(final Tweet t, final String filter) {
        super(t);
        keyword = filter;
    } // InsertKeywordSQLTask(Tweet, String)

    @Override
    public int doTask(final DatabaseConnector db) {
        return doSqlTask((SqlConnector) db);
    } // doTask(DatabaseConnector)

    // This needs fixing to fit new structure
    public int doSqlTask(final SqlConnector sql) {
        final Tweet tweet = getTweet();
        return sql.insertTweet(tweet.getId(), tweet.getScreenName(), tweet.getTweet(),
                tweet.getCreatedAt(), tweet.getUserId(), keyword);
    } // doSqlTask(SqlConnector)
} // InsertKeywordSQLTask
