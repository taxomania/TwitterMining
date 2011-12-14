package uk.ac.manchester.cs.patelt9.twitter.gui.tablemodel;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TweetDbTable extends DbTable {
    private static final long serialVersionUID = -2366349712354391578L;
    private ResultSet rs;
    private int index = 0;

    //@formatter:off
    private static final String QUERY_PREFIX = "SELECT u.username, t.text, "
            + "t.created_at, t.sentiment FROM tweet t, user u ";
    private static final String META_QUERY = QUERY_PREFIX + "LIMIT 1";
    private static final String DEFAULT_QUERY = QUERY_PREFIX
            + "WHERE u.id = t.user_id ORDER BY t.id LIMIT 1000;";
    //@formatter:on

    public TweetDbTable() throws SQLException {
        super(META_QUERY);
    } // TweetDbTable()

    @Override
    protected String getQuery() {
        return DEFAULT_QUERY;
    } // getQuery()

    @Override
    public void next() throws SQLException {
        super.next();
        int i = 0;
        for (; (i < 100) && rs.next(); i++) {
            addElements();
        } // while
        index += i;
    } // next()

    private void addElements() throws SQLException {
        final String[] record = new String[colCount];
        for (int i = 0; i < colCount; i++) {
            record[i] = rs.getString(headers[i]);
        } // for
        cache.addElement(record);
    } // addElements()

    @Override
    public void previous() throws SQLException {
        super.previous();
        int i = 0;
        for (; (i < 100) && rs.previous(); i++) {
            addElements();
        } // while
        index -= i;
    } // previous()

    @Override
    protected void update() throws SQLException {
        rs = sql.executeQuery(getQuery());
        rs.beforeFirst();

        for (index = 0; rs.next() && (index < 100); index++) {
            addElements();
        } // for
    }
} // TweetDbTable
