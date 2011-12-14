package uk.ac.manchester.cs.patelt9.twitter.gui.swing;

import java.awt.Dimension;
import java.sql.SQLException;

import uk.ac.manchester.cs.patelt9.twitter.gui.tablemodel.UserDbTable;

public final class UserDbPanel extends DbTablePanel {
    private static final long serialVersionUID = -7548894886086628117L;
    private static final int WIDTH = 150;
    private static final int HEIGHT = 660;

    public UserDbPanel() throws SQLException {
        this(new UserDbTable());
    } // UserDbPanel()

    public UserDbPanel(final Dimension d) throws SQLException {
        this(new UserDbTable(), d);
    } // UserDbPanel(Dimension)

    public UserDbPanel(final UserDbTable tbl, final Dimension d) throws SQLException {
        super(tbl, "All tweets", d);
    } // UserDbPanel(UserDbTable, Dimension)


    public UserDbPanel(final UserDbTable tbl) {
        super(tbl, "All users", new Dimension(WIDTH, HEIGHT));
    } // UserDbPanel(UserDbTable)
} // UserDbPanel()