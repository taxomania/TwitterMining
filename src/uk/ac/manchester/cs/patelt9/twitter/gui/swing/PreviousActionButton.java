package uk.ac.manchester.cs.patelt9.twitter.gui.swing;

import javax.swing.JButton;

import uk.ac.manchester.cs.patelt9.twitter.gui.listeners.PreviousActionListener;
import uk.ac.manchester.cs.patelt9.twitter.gui.swing.table.DbTable;

public final class PreviousActionButton extends JButton {
    private static final long serialVersionUID = -5932783149121611068L;

    public PreviousActionButton(final DbTable tbl) {
        super("Previous");
        this.addActionListener(new PreviousActionListener(tbl));
    } // PreviousActionButton(DbTable)
} // PreviousActionButton