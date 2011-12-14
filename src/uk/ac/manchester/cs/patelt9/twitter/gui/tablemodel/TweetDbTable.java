package uk.ac.manchester.cs.patelt9.twitter.gui.tablemodel;

import java.sql.SQLException;

public class TweetDbTable extends DbTable {
    private static final long serialVersionUID = -2366349712354391578L;
    private static final int MAX_RESULTS = 100;

    //@formatter:off
    private static final String QUERY_PREFIX = "SELECT u.username, t.text, "
            + "t.created_at, t.sentiment FROM tweet t, user u ";
    private static final String META_QUERY = QUERY_PREFIX + "LIMIT 1";
    private static final String DEFAULT_QUERY_PREFIX = QUERY_PREFIX
            + "WHERE u.id = t.user_id ORDER BY t.id DESC LIMIT ";
    //@formatter:on

    public TweetDbTable() throws SQLException {
        super(META_QUERY);
    } // TweetDbTable()

    @Override
    protected String getQuery() {
        return DEFAULT_QUERY_PREFIX + (page * MAX_RESULTS) + ", " + MAX_RESULTS;
    } // getQuery()
} // TweetDbTable
