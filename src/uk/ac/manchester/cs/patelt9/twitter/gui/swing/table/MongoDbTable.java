package uk.ac.manchester.cs.patelt9.twitter.gui.swing.table;

import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import uk.ac.manchester.cs.patelt9.twitter.data.db.SoftwareMongoConnector;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

public final class MongoDbTable extends AbstractTableModel {
    private static final long serialVersionUID = -8921742154153487037L;
    private final SoftwareMongoConnector mongo;
    private final Set<String> keys = new HashSet<String>();
    private final Vector<String[]> cache = new Vector<String[]>();
    private int colCount;
    private String[] headers;

    public MongoDbTable() throws UnknownHostException, MongoException {
        mongo = SoftwareMongoConnector.getInstance();
        refresh();
    } // MongoDbTable(String)

    private void loadHeaders(final Iterator<DBObject> ite) {
        while (ite.hasNext()) {
            final DBObject obj = ite.next();
            keys.addAll(obj.keySet());
        } // while()
        colCount = keys.size();

        headers = new String[colCount];

        final Iterator<String> it = keys.iterator();
        for (int i = 0; it.hasNext(); i++) {
            headers[i] = it.next();
        } // for
    }// loadHeaders()

    public final void refresh() {
        final DBCursor c = mongo.selectAll();
        loadHeaders(c.iterator());
        cache.clear();

        while (c.hasNext()) {
            final DBObject d = c.next();
            final String[] record = new String[colCount];
            for (int i = 0; i < colCount; i++) {
                try {
                    record[i] = (String) d.get(headers[i]).toString();
                } catch (final NullPointerException e) {
                    record[i] = "";
                } // catch
            } // for
            cache.addElement(record);
        } // while
        fireTableChanged(null);
    } // refresh()

    @Override
    public int getColumnCount() {
        return colCount;
    } // getColumnCount()

    @Override
    public int getRowCount() {
        return cache.size();
    } // getRowCount()

    @Override
    public String getColumnName(final int column) {
        return headers[column];
    } // getColumnName(int)

    @Override
    public Object getValueAt(final int row, final int col) {
        return ((String[]) cache.elementAt(row))[col];
    } // getValueAt(int, int)
} // MongoDbTable
