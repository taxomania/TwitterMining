package uk.ac.manchester.cs.patelt9.twitter.gui.swing;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.sql.SQLException;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import uk.ac.manchester.cs.patelt9.twitter.gui.swing.table.DbTable;

public class DbTablePanel extends JPanel {
    private static final long serialVersionUID = -8909541940637552591L;
    private final DbTable tbl;

    public DbTablePanel(final DbTable tbl, String title) {
        this.tbl = tbl;
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(new JLabel(title));
    } // DbTablePanel

    public DbTablePanel(final DbTable tbl, String title, Dimension d) {
        this(tbl, title);
        addTable(d);
        addButtons();
    } // DbTablePanel

    public void addTable(final Dimension d) {
        final JScrollPane scroll = new JScrollPane(new JTable(tbl));
        scroll.setPreferredSize(d);
        this.add(scroll);
    } // addTable(Dimension)

    public void addButtons() {
        final JPanel buttons = new JPanel(new FlowLayout());
        buttons.add(new PreviousActionButton(tbl));
        buttons.add(new NextActionButton(tbl));
        this.add(buttons);
    } // addButtons()

    public void refresh() throws SQLException {
        tbl.refresh();
    } // refresh()
} // DbTablePanel
