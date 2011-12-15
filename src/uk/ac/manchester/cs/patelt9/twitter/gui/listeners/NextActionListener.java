package uk.ac.manchester.cs.patelt9.twitter.gui.listeners;

import java.sql.SQLException;

import uk.ac.manchester.cs.patelt9.twitter.gui.swing.table.DbTable;

public final class NextActionListener extends DbTableActionListener {
    public NextActionListener(final DbTable tbl) {
        super(tbl);
    } // NextActionListener

    @Override
    protected void actionPerformed() throws SQLException {
        tbl.next();
    } // actionPerformed()
} // NextActionListener
