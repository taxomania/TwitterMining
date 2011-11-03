package uk.ac.manchester.cs.patelt9.twitter.data;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import uk.ac.manchester.cs.patelt9.twitter.ScannerThread;
import uk.ac.manchester.cs.patelt9.twitter.parse.SentimentParseThread;
import uk.ac.manchester.cs.patelt9.twitter.parse.SentimentParseThread.ParseListener;

import com.alchemyapi.api.AlchemyAPI;

public class SentimentAnalysis implements ParseListener {
    private static final String DEFAULT_QUERY = "SELECT text, id FROM tweet WHERE sentiment IS NULL LIMIT 30000;";

    private final AlchemyAPI api;
    private ResultSet res;
    private volatile SqlConnector sql = null;
    private int count = 0;

    private static SentimentAnalysis sa = null;

    public static SentimentAnalysis getInstance() throws IOException, SQLException {
        if (sa == null) {
            sa = new SentimentAnalysis();
        } // if
        return sa;
    } // getInstance()

    private SentimentAnalysis() throws IOException, SQLException {
        api = AlchemyAPI.GetInstanceFromFile("alchemyapikey.txt");
        sql = SqlConnector.getInstance();

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
        try {
            new ScannerThread() {
                @Override
                protected void performTask() {
                    stillAnalyse = false;
                } // performTask()
            }.start();
            res.beforeFirst();
            while (stillAnalyse && res.next()) {
                final String tweet = res.getString(1);
                final Long id = res.getLong(2);
                // System.out.println(id + ": " + tweet);
                try {
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
                    } // catch
                    System.out.println(Thread.currentThread().getName());
                    parseThread = new SentimentParseThread(id, doc);
                    parseThread.addListener(this);
                    parseThread.start();
                } catch (final IOException e) {
                    updateError(id);
                    continue;
                } // catch
            } // while
            close();
        } catch (final DOMException e) {
            e.printStackTrace();
        } catch (final SQLException e) {
            e.printStackTrace();
        } // catch
    } // analyseSentiment()

    private void updateError(final long id){
        count += sql.executeUpdate("UPDATE tweet SET sentiment='error' WHERE id='"
                + id + "';");
    } // updateError(long)

    private void close() {
        if (parseThread != null) {
            try {
                parseThread.join();
                parseThread.removeListener(this);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            } // catch
        } // if
        System.out.println(Integer.toString(count));
        if (sql != null) {
            sql.close();
        } // if
        sa = null;
    } // close()

    @Override
    public void onParseComplete(final long id, final String sentiment) {
        System.out.println(Thread.currentThread().getName());
        count += sql.executeUpdate("UPDATE tweet SET sentiment='" + sentiment + "' WHERE id='" + id
                + "';");
    } // onParseComplete(long, String)

    @Override
    public void onParseComplete(final long id, final String sentiment, final String sentimentScore) {
        System.out.println(Thread.currentThread().getName());
        count += sql.executeUpdate("UPDATE tweet SET sentiment='" + sentiment
                + "', sentiment_score='" + sentimentScore + "' WHERE id='" + id + "';");
    } // onParseComplete(long, String, String)
} // SentimentAnalysis
