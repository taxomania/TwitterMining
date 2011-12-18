package uk.ac.manchester.cs.patelt9.twitter.gui.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

import uk.ac.manchester.cs.patelt9.twitter.gui.swing.table.DbTable;

public abstract class DbTableActionListener implements ActionListener {
    protected final DbTable tbl;

    public DbTableActionListener(final DbTable tbl) {
        this.tbl = tbl;
    } // DbTableActionListener(DbTable)

    @Override
    public final void actionPerformed(final ActionEvent e) {
        try {
            actionPerformed();
        } catch (final SQLException e1) {
            e1.printStackTrace();
        } // catch
    } // actionPerformed(ActionEvent)

    protected abstract void actionPerformed() throws SQLException;
} // DbTableActionListener
