package uk.ac.manchester.cs.patelt9.twitter.data.sqltask;

import uk.ac.manchester.cs.patelt9.twitter.data.SqlConnector;
import uk.ac.manchester.cs.patelt9.twitter.data.Tweet;

public class InsertSQLTask extends SQLTask {
    private final Tweet tweet;

    public InsertSQLTask(final Tweet t) {
        tweet = t;
    } // InsertSQLTask(Tweet)

    protected Tweet getTweet(){
        return tweet;
    } // getTweet()

    @Override
    public int doSqlTask(final SqlConnector sql) {
        return sql.insertTweet(tweet.getId(), tweet.getScreenName(), tweet.getTweet(),
                tweet.getCreatedAt(), tweet.getUserId());
    } // doSqlTask(SqlConnector)

    @Override
    public String toString() {
        return tweet.toString();
    } // toString()
} // InsertSQLTask
