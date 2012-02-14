package uk.ac.manchester.cs.patelt9.twitter.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import uk.ac.manchester.cs.patelt9.twitter.gui.swing.table.MongoDbTable;

import com.mongodb.MongoException;

public class MongoDatabaseGUI extends JFrame {
    private static final long serialVersionUID = 5393049585293383018L;

    private final JTextArea software = new JTextArea();

    public MongoDatabaseGUI() throws UnknownHostException, MongoException {
        super("TwitterMining Mongo Database");
        init();
    } // MongoDatabaseGUI()

    private void init() throws UnknownHostException, MongoException {
        JFrame.setDefaultLookAndFeelDecorated(true);
        final Container contents = getContentPane();
        contents.setLayout(new BorderLayout());
        software.setEditable(false);
        software.setFont(new Font("Verdana", Font.PLAIN, 12));

        final MongoDbTable tbl = new MongoDbTable();
        contents.add(new JScrollPane(new JTable(tbl)), BorderLayout.CENTER);

        final JButton update = new JButton("Refresh All");
        update.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                tbl.refresh();
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
                    new MongoDatabaseGUI().setVisible(true);
                } catch (final UnknownHostException e) {
                    e.printStackTrace();
                } catch (final MongoException e) {
                    e.printStackTrace();
                } // catch
            } // run()
        });
    } // main(String[])
} // MongoDatabaseGUI
