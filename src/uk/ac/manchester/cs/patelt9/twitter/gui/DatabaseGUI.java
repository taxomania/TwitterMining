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

    private void init() throws SQLException {
        JFrame.setDefaultLookAndFeelDecorated(true);
        final Container contents = getContentPane();
        contents.setLayout(new BorderLayout());

        final DbTable userDb = new UserDbTable();
        final DbTable tweetDb = new TweetDbTable();

        final JPanel jpanel = new JPanel(new FlowLayout());
        JTable table = new JTable(userDb);
        JScrollPane scroller = new JScrollPane(table);
        scroller.setPreferredSize(new Dimension(150, 700));
        jpanel.add(scroller);

        final JPanel tweets = new JPanel();
        tweets.setLayout(new BoxLayout(tweets, BoxLayout.Y_AXIS));
        table = new JTable(tweetDb);
        scroller = new JScrollPane(table);
        scroller.setPreferredSize(new Dimension(950, 660));
        tweets.add(scroller);

        final JPanel buttons = new JPanel(new FlowLayout());
        final JButton previous = new JButton("Previous");
        previous.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {
                    tweetDb.previous();
                } catch (final SQLException e) {
                    e.printStackTrace();
                }
            }
        });
        buttons.add(previous);
        final JButton next = new JButton("Next");
        next.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {
                    tweetDb.next();
                } catch (final SQLException e) {
                    e.printStackTrace();
                }
            }
        });
        buttons.add(next);
        tweets.add(buttons);
        jpanel.add(tweets);
        contents.add(jpanel, BorderLayout.CENTER);

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
