package uk.ac.manchester.cs.patelt9.twitter.data;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import uk.ac.manchester.cs.patelt9.twitter.parse.ScannerThread;
import uk.ac.manchester.cs.patelt9.twitter.parse.SentimentParseThread;
import uk.ac.manchester.cs.patelt9.twitter.parse.SentimentParseThread.ParseListener;

import com.alchemyapi.api.AlchemyAPI;

public class SentimentAnalysis implements ParseListener {
    private static final String DEFAULT_QUERY = "SELECT text, id FROM tweet WHERE sentiment IS NULL LIMIT 30000;";

    private final AlchemyAPI api;
    private ResultSet res;
    private volatile SqlConnector sql = null;
    private int count = 0;
    private ScannerThread scanner = null;

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

    private SentimentParseThread parseThread = null;
    private volatile boolean stillAnalyse = true;

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

    private void updateError(final long id) {
        count += sql.updateSentiment("error", id);
    } // updateError(long)

    private boolean isScanner() {
        return scanner != null;
    } // isScanner()

    public void close() {
        if (isScanner()) {
            if (parseThread != null) {
                try {
                    parseThread.join();
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                } // catch
                parseThread.removeListener(this);
            } // if
            if (scanner != null && scanner.isAlive()) {
                scanner.interrupt();
                scanner = null;
            } // if
            sql.close();
        } // if
        System.out.println(Integer.toString(count) + " tweets analysed");
        sa = null;
    } // close()

    public void closeSql() {
        if (sql != null) {
            sql.close();
        } // if
    } // closeSql()

    @Override
    public void onParseComplete(final long id, final String sentiment) {
        count += sql.updateSentiment(sentiment, id);
    } // onParseComplete(long, String)

    @Override
    public void onParseComplete(final long id, final String sentiment, final String sentimentScore) {
        count += sql.updateSentimentScore(sentiment, sentimentScore, id);
    } // onParseComplete(long, String, String)
} // SentimentAnalysis
