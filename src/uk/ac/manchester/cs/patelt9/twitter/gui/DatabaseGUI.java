package uk.ac.manchester.cs.patelt9.twitter.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import uk.ac.manchester.cs.patelt9.twitter.gui.swing.DbTablePanel;
import uk.ac.manchester.cs.patelt9.twitter.gui.tablemodel.TweetDbTable;
import uk.ac.manchester.cs.patelt9.twitter.gui.tablemodel.UserDbTable;

public class DatabaseGUI extends JFrame {
    private static final long serialVersionUID = 5393049585293383058L;

    public DatabaseGUI() throws SQLException {
        super("Twitter Statistics");
        init();
    } // DatabaseGUI()

    private static final class UserDbPanel extends DbTablePanel {
        private static final long serialVersionUID = -7548894886086628117L;
        private static final int WIDTH = 150;
        private static final int HEIGHT = 660;

        public UserDbPanel(final UserDbTable tbl) {
            super(tbl, "All users", new Dimension(WIDTH, HEIGHT));
        } // UserDbPanel(UserDbTable)
    } // UserDbPanel()

    private static final class TweetDbPanel extends DbTablePanel {
        private static final long serialVersionUID = -7455757631429134627L;
        private static final int WIDTH = 950;
        private static final int HEIGHT = 660;

        public TweetDbPanel(final TweetDbTable tbl) {
            super(tbl, "All tweets", new Dimension(WIDTH, HEIGHT));
        } // TweetDbPanel(TweetDbTable)
    } // TweetDbPanel()

    private void init() throws SQLException {
        JFrame.setDefaultLookAndFeelDecorated(true);
        final Container contents = getContentPane();
        contents.setLayout(new BorderLayout());

        final JPanel mainPanel = new JPanel(new FlowLayout());

        final UserDbTable userDb = new UserDbTable();
        mainPanel.add(new UserDbPanel(userDb));

        final TweetDbTable tweetDb = new TweetDbTable();
        mainPanel.add(new TweetDbPanel(tweetDb));

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
