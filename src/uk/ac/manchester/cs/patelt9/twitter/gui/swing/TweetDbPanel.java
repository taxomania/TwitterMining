package uk.ac.manchester.cs.patelt9.twitter.gui.swing;

import java.awt.Dimension;
import java.sql.SQLException;

import uk.ac.manchester.cs.patelt9.twitter.gui.tablemodel.TweetDbTable;

public final class TweetDbPanel extends DbTablePanel {
    private static final long serialVersionUID = -7455757631429134627L;
    private static final int WIDTH = 950;
    private static final int HEIGHT = 660;

    public TweetDbPanel() throws SQLException {
        this(new TweetDbTable());
    } // TweetDbPanel()

    public TweetDbPanel(final TweetDbTable tbl) {
        super(tbl, "All tweets", new Dimension(WIDTH, HEIGHT));
    } // TweetDbPanel(TweetDbTable)
} // TweetDbPanel()
