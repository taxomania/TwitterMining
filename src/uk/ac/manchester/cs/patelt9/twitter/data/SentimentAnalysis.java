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
        System.out.println("Analysing tweet sentiment");
        new ScannerThread() {
            @Override
            protected void performTask() {
                stillAnalyse = false;
            } // performTask()
        }.start();
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
                    } else {
                        updateError(id);
                    } // else
                    continue;
                } // catch
                parseThread = new SentimentParseThread(id, doc);
                parseThread.addListener(this);
                parseThread.start();
            } // while
            close();
        } catch (final SQLException e) {
            e.printStackTrace();
        } // catch
    } // analyseSentiment()

    private void updateError(final long id) {
        count += sql.updateSentiment("error", id);
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
        System.out.println(Integer.toString(count) + " tweets analysed");
        if (sql != null) {
            sql.close();
        } // if
        sa = null;
    } // close()

    @Override
    public void onParseComplete(final long id, final String sentiment) {
        count += sql.updateSentiment(sentiment, id);
    } // onParseComplete(long, String)

    @Override
    public void onParseComplete(final long id, final String sentiment, final String sentimentScore) {
        count += sql.updateSentimentScore(sentiment, sentimentScore, id);
    } // onParseComplete(long, String, String)
} // SentimentAnalysis
