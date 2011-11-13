package uk.ac.manchester.cs.patelt9.twitter.data.sql;

import uk.ac.manchester.cs.patelt9.twitter.data.SqlConnector;
import uk.ac.manchester.cs.patelt9.twitter.data.Tweet;

public class InsertSQLTask extends SQLTask {
    protected final Tweet tweet;

    public InsertSQLTask(final Tweet t) {
        tweet = t;
    } // InsertSQLTask(Tweet)

    @Override
    protected int doSqlTask(final SqlConnector sql) {
        return sql.insertTweet(tweet.getId(), tweet.getScreenName(), tweet.getTweet(),
                tweet.getCreatedAt(), tweet.getUserId());
    } // doSqlTask(SqlConnector)
} // InsertSQLTask
