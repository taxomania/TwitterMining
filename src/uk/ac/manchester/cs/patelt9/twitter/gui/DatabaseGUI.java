package uk.ac.manchester.cs.patelt9.twitter.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import uk.ac.manchester.cs.patelt9.twitter.gui.tablemodel.DbTable;
import uk.ac.manchester.cs.patelt9.twitter.gui.tablemodel.TweetDbTable;
import uk.ac.manchester.cs.patelt9.twitter.gui.tablemodel.UserDbTable;

public class DatabaseGUI extends JFrame {
    private static final long serialVersionUID = 5393049585293383058L;

    public DatabaseGUI() throws SQLException {
        super("Twitter Statistics");
        init();
    } // DatabaseGUI()

    private static abstract class DbTableActionListener implements ActionListener {
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

    private static final class PreviousAction extends DbTableActionListener {
        public PreviousAction(final DbTable tbl) {
            super(tbl);
        } // NextAction

        @Override
        protected void actionPerformed() throws SQLException {
            tbl.previous();
        } // actionPerformed()
    } // PreviousAction

    private static final class NextAction extends DbTableActionListener {
        public NextAction(final DbTable tbl) {
            super(tbl);
        } // NextAction

        @Override
        protected void actionPerformed() throws SQLException {
            tbl.next();
        } // actionPerformed()
    } // NextAction

    private static final class DbTablePanel extends JPanel {
        private static final long serialVersionUID = -8909541940637552591L;
        private final DbTable tbl;

        public DbTablePanel(final DbTable tbl, String title) {
            this.tbl = tbl;
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            this.add(new JLabel(title));
        } // DbTablePanel

        public void addTable(final Dimension d) {
            final JScrollPane scroll = new JScrollPane(new JTable(tbl));
            scroll.setPreferredSize(d);
            this.add(scroll);
        } // addTable(Dimension)

        public void addButtons() {
            final JPanel buttons = new JPanel(new FlowLayout());
            JButton button = new JButton("Previous");
            button.addActionListener(new PreviousAction(tbl));
            buttons.add(button);
            button = new JButton("Next");
            button.addActionListener(new NextAction(tbl));
            buttons.add(button);
            this.add(buttons);
        } // addButtons()
    } // DbTablePanel

    private void init() throws SQLException {
        JFrame.setDefaultLookAndFeelDecorated(true);
        final Container contents = getContentPane();
        contents.setLayout(new BorderLayout());

        final DbTable userDb = new UserDbTable();
        final DbTable tweetDb = new TweetDbTable();

        final JPanel mainPanel = new JPanel(new FlowLayout());

        final DbTablePanel tbl = new DbTablePanel(userDb, "All users");
        tbl.addTable(new Dimension(150, 660));
        tbl.addButtons();
        mainPanel.add(tbl);

        final DbTablePanel tbl2 = new DbTablePanel(tweetDb, "All tweets");
        tbl2.addTable(new Dimension(950, 660));
        tbl2.addButtons();
        mainPanel.add(tbl2);

        contents.add(mainPanel, BorderLayout.CENTER);

        final JButton update = new JButton("Refresh All");
        update.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                try {
                    userDb.refresh();
                    tweetDb.refresh();
                } catch (final SQLException e) {
                    e.printStackTrace();
                } // catch
            } // actionPerformed(ActionEvent)
        });
        contents.add(update, BorderLayout.SOUTH);

        // To make sure the default operation on pressing the close button on the window-bar
        // is to exit the program.
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // To size the window to fit all the widgets in, without working out the size manually.
        pack();
    } // init()

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    new DatabaseGUI().setVisible(true);
                } catch (final SQLException e) {
                    e.printStackTrace();
                } // catch
            } // run()
        });
    } // main(String[])

} // DatabaseGUI
