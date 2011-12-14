package uk.ac.manchester.cs.patelt9.twitter.gui.tablemodel;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import uk.ac.manchester.cs.patelt9.twitter.data.db.TweetSQLConnector;

public abstract class DbTable extends AbstractTableModel {
    private static final long serialVersionUID = -8921742854153487037L;
    protected final String[] headers;
    protected final Vector<String[]> cache = new Vector<String[]>();
    protected final int colCount;
    protected final TweetSQLConnector sql;
    protected int page = 0;

    protected DbTable(final String metaQuery) throws SQLException {
        sql = TweetSQLConnector.getInstance();
        final ResultSetMetaData meta = sql.executeQuery(metaQuery).getMetaData();
        colCount = meta.getColumnCount();

        headers = new String[colCount];
        for (int i = 1; i <= colCount; i++) {
            headers[i - 1] = meta.getColumnName(i);
        } // for
        refresh();
    } // DbTable(String)

    public final void refresh() throws SQLException {
        cache.clear();
        update();
        fireTableChanged(null);
    } // refresh()

    protected void update() throws SQLException {
        final ResultSet rs = sql.executeQuery(getQuery());
        rs.beforeFirst();
        while (rs.next()) {
            final String[] record = new String[colCount];
            for (int i = 0; i < colCount; i++) {
                record[i] = rs.getString(headers[i]);
            } // for
            cache.addElement(record);
        } // while
    } // update()

    protected abstract String getQuery();

    public final void next() throws SQLException {
        page++;
        refresh();
    } // next()

    public final void previous() throws SQLException {
        if (page == 0) return;
        page--;
        refresh();
    } // previous()

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
} // DbTable