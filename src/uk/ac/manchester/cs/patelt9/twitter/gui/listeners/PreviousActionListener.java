package uk.ac.manchester.cs.patelt9.twitter.gui.listeners;

import java.sql.SQLException;

import uk.ac.manchester.cs.patelt9.twitter.gui.swing.table.DbTable;

public final class PreviousActionListener extends DbTableActionListener {
    public PreviousActionListener(final DbTable tbl) {
        super(tbl);
    } // PreviousActionListener

    @Override
    protected void actionPerformed() throws SQLException {
        tbl.previous();
    } // actionPerformed()
} // PreviousActionListener

