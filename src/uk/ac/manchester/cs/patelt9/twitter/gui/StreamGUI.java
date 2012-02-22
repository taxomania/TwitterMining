package uk.ac.manchester.cs.patelt9.twitter.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import uk.ac.manchester.cs.patelt9.twitter.gui.swing.table.MongoDbTable;

import com.mongodb.MongoException;

public class StreamGUI extends JFrame {
    private static final long serialVersionUID = 1393049585293383018L;

    private final JTextArea text = new JTextArea();

    public StreamGUI() {
        super("TwitterMining Streaming");
        init();
    } // StreamGUI()

    private final class QueryDialog extends JDialog {

    }

    private void init() {
        JFrame.setDefaultLookAndFeelDecorated(true);
        final Container contents = getContentPane();
        contents.setLayout(new BorderLayout());
        text.setEditable(false);
        text.setFont(new Font("Verdana", Font.PLAIN, 12));

        final JPanel topPanel = new JPanel(new FlowLayout());
        final JButton search = new JButton("Query");
        search.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub

            }
        });
        topPanel.add(search);

        final JButton dict = new JButton("Use Dictionary");
        dict.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub

            }
        });
        topPanel.add(dict);

        contents.add(topPanel, BorderLayout.NORTH);
        final MongoDbTable tbl = null;
        // contents.add(new JScrollPane(new JTable(tbl)), BorderLayout.CENTER);

        final JButton update = new JButton("Refresh All");
        update.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
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
                new StreamGUI().setVisible(true);
            } // run()
        });
    } // main(String[])
} // MongoDatabaseGUI
