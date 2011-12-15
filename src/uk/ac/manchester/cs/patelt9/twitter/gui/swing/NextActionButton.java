package uk.ac.manchester.cs.patelt9.twitter.gui.swing;

import javax.swing.JButton;

import uk.ac.manchester.cs.patelt9.twitter.gui.listeners.NextActionListener;
import uk.ac.manchester.cs.patelt9.twitter.gui.swing.table.DbTable;

public final class NextActionButton extends JButton {
    private static final long serialVersionUID = 3416306495461258311L;

    public NextActionButton(final DbTable tbl) {
        super("Next");
        this.addActionListener(new NextActionListener(tbl));
    } // NextActionButton(DbTable)
} // NextActionButton