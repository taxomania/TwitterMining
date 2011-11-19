package uk.ac.manchester.cs.patelt9.twitter.data.sqltask;

import uk.ac.manchester.cs.patelt9.twitter.data.SqlConnector;
import uk.ac.manchester.cs.patelt9.twitter.data.Tweet;
import uk.ac.manchester.cs.patelt9.twitter.data.task.InsertTask;

public class InsertSQLTask extends InsertTask implements SQLTask {
    public InsertSQLTask(final Tweet t) {
        super(t);
    } // InsertSQLTask(Tweet)

    @Override
    public int doSqlTask(final SqlConnector sql) {
        final Tweet tweet = getTweet();
        return sql.insertTweet(tweet.getId(), tweet.getScreenName(), tweet.getTweet(),
                tweet.getCreatedAt(), tweet.getUserId());
    } // doSqlTask(SqlConnector)
} // InsertSQLTask
