package uk.ac.manchester.cs.patelt9.twitter.gui.tablemodel;

import java.sql.SQLException;

public class UserDbTable extends DbTable {
    private static final long serialVersionUID = 7950387004859697480L;

    private static final String DEFAULT_QUERY = "SELECT username FROM user ORDER BY username";
    private static final String META_QUERY = DEFAULT_QUERY + " LIMIT 1";

    public UserDbTable() throws SQLException {
        super(META_QUERY);
    } // UserDbTable()

    @Override
    protected String getQuery() {
        return DEFAULT_QUERY;
    } // getQuery()
} // UserDbTable
