package uk.ac.manchester.cs.patelt9.twitter.data;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import uk.ac.manchester.cs.patelt9.twitter.data.SqlThread.SqlTaskCompleteListener;
import uk.ac.manchester.cs.patelt9.twitter.parse.ScannerThread;
import uk.ac.manchester.cs.patelt9.twitter.parse.SentimentParseThread;
import uk.ac.manchester.cs.patelt9.twitter.parse.SentimentParseThread.ParseListener;

import com.alchemyapi.api.AlchemyAPI;

public class SentimentAnalysis implements ParseListener, SqlTaskCompleteListener {
    private static final String DEFAULT_QUERY = "SELECT text, id FROM tweet WHERE sentiment IS NULL LIMIT 30000;";

    private final AlchemyAPI api;
    private ResultSet res;
    private volatile SqlConnector sql = null;
    private volatile boolean stillAnalyse = true;
    private int count = 0;
    private ScannerThread scanner = null;
    private SqlThread sqlThread = null;
    private SentimentParseThread parseThread = null;

    private static SentimentAnalysis sa = null;

    public static SentimentAnalysis getInstance() throws IOException, SQLException {
        if (sa == null) {
            sa = new SentimentAnalysis();
        } // if
        return sa;
    } // getInstance()

    public static SentimentAnalysis getInstance(final SqlConnector sql) throws IOException,
            SQLException {
        if (sa == null) {
            sa = new SentimentAnalysis(sql);
        } // if
        return sa;
    } // getInstance()

    private SentimentAnalysis(final SqlConnector sql) throws IOException {
        api = AlchemyAPI.GetInstanceFromFile("alchemyapikey.txt");
        this.sql = sql;
    } // SentimentAnalysis(SqlConnector)

    private SentimentAnalysis() throws IOException, SQLException {
        this(SqlConnector.getInstance());
        scanner = new ScannerThread() {
            @Override
            protected void performTask() {
                stillAnalyse = false;
            } // performTask()
        };
    } // SentimentAnalysis()

    private void setRes(final ResultSet res) {
        this.res = res;
    } // setRes(ResultSet)

    public void loadDataSet() {
        loadDataSet(DEFAULT_QUERY);
    } // loadDataSet()

    public void loadDataSet(final String sqlStatement) {
        try {
            setRes(sql.executeQuery(sqlStatement));
        } catch (final SQLException e) {
            e.printStackTrace();
        } // catch
    } // loadDataSet(String)

    public void analyseSentiment() {
        System.out.println("Analysing tweet sentiment");
        if (isScanner()) {
            scanner.start();
        } // if
        try {
            res.beforeFirst();
            while (stillAnalyse && res.next()) {
                final String tweet = res.getString(1);
                final Long id = res.getLong(2);
                // System.out.println(id + ": " + tweet);
                final Document doc;
                try {
                    doc = api.TextGetTextSentiment(tweet);
                } catch (final IllegalArgumentException e) {
                    updateError(id);
                    continue;
                } catch (final SAXException e) {
                    e.printStackTrace();
                    continue;
                } catch (final XPathExpressionException e) {
                    e.printStackTrace();
                    continue;
                } catch (final ParserConfigurationException e) {
                    e.printStackTrace();
                    continue;
                } catch (final IOException e) {
                    if (e.getMessage().contains("limit")) {
                        System.out.println(e.getMessage());
                        if (isScanner()) {
                            scanner.interrupt();
                        } // if
                        break;
                    } else {
                        updateError(id);
                        continue;
                    } // else
                } // catch
                parseThread = new SentimentParseThread(id, doc);
                parseThread.addListener(this);
                parseThread.start();
            } // while
        } catch (final SQLException e) {
            e.printStackTrace();
        } // catch
    } // analyseSentiment()

    private boolean isScanner() {
        return scanner != null;
    } // isScanner()

    public void close() {
        if (parseThread != null) {
            try {
                parseThread.join();
            } catch (final InterruptedException e) {
                e.printStackTrace();
            } // catch
            parseThread.removeListener(this);
        } // if
        if (isScanner()) {
            if (scanner.isAlive()) {
                scanner.interrupt();
                scanner = null;
            } // if
        } // if
        if (sqlThread != null && sqlThread.isAlive()) {
            try {
                sqlThread.join();
            } catch (final InterruptedException e) {
                e.printStackTrace();
            } // catch
            sqlThread.removeListener(this);
        } // if
        if (isScanner()) {
            closeSql();
        } // if
        System.out.println(Integer.toString(count) + " tweets analysed");
        sa = null;
    } // close()

    private void closeSql() {
        if (sql != null) {
            sql.close();
        } // if
    } // closeSql()

    @Override
    public void onParseComplete(final long id, final String sentiment) {
        sqlThread = new SqlThread() {
            @Override
            protected void performTask() {
                notifyListeners(sql.updateSentiment(sentiment, id));
            } // performTask();
        };
        sqlThread.addListener(this);
        sqlThread.start();
    } // onParseComplete(long, String)

    @Override
    public void onParseComplete(final long id, final String sentiment, final String sentimentScore) {
        sqlThread = new SqlThread() {
            @Override
            protected void performTask() {
                notifyListeners(sql.updateSentimentScore(sentiment, sentimentScore, id));
            } // performTask();
        };
        sqlThread.addListener(this);
        sqlThread.start();
    } // onParseComplete(long, String, String)

    private void updateError(final long id) {
        sqlThread = new SqlThread() {
            @Override
            protected void performTask() {
                notifyListeners(sql.updateSentiment("error", id));
            } // performTask();
        };
        sqlThread.addListener(this);
        sqlThread.start();
    } // updateError(long)

    @Override
    public void onSqlTaskComplete(final int rowsAffected) {
        count += rowsAffected;
    } // onSqlTaskComplete(int)
} // SentimentAnalysis
