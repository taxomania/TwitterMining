package uk.ac.manchester.cs.patelt9.twitter.data;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import uk.ac.manchester.cs.patelt9.twitter.data.sqltask.DeleteTweetSQLTask;
import uk.ac.manchester.cs.patelt9.twitter.data.sqltask.SentimentSQLTask;
import uk.ac.manchester.cs.patelt9.twitter.data.sqltask.SentimentScoreSQLTask;
import uk.ac.manchester.cs.patelt9.twitter.parse.ScannerThread;
import uk.ac.manchester.cs.patelt9.twitter.practice.SentimentParseThread;
import uk.ac.manchester.cs.patelt9.twitter.practice.SentimentParseThread.ParseListener;

import com.alchemyapi.api.AlchemyAPI;

public class SentimentAnalysis implements ParseListener {
    private static final String DEFAULT_QUERY = "SELECT text, id FROM tweet WHERE sentiment IS NULL"
            + " AND keyword IS NOT NULL LIMIT 30000;";

    private final AlchemyAPI api;
    private ResultSet res;
    private boolean stillAnalyse = true;
    private ScannerThread scanner = null;
    private SentimentParseThread parseThread = null;
    private SQLThread sqlThread = null;

    private static SentimentAnalysis sa = null;

    public static SentimentAnalysis getInstance() throws IOException, SQLException {
        if (sa == null) {
            sa = new SentimentAnalysis();
        } // if
        return sa;
    } // getInstance()

    private SentimentAnalysis() throws IOException, SQLException {
        api = AlchemyAPI.GetInstanceFromFile("alchemyapikey.txt");
        scanner = new ScannerThread() {
            @Override
            protected void performTask() {
                stillAnalyse = false;
            } // performTask()
        };
        sqlThread = new SQLThread();
    } // SentimentAnalysis()

    private void setRes(final ResultSet res) {
        this.res = res;
    } // setRes(ResultSet)

    public void loadDataSet() {
        loadDataSet(DEFAULT_QUERY);
    } // loadDataSet()

    public void loadDataSet(final String sqlStatement) {
        try {
            setRes(SqlConnector.getInstance().executeQuery(sqlStatement));
        } catch (final SQLException e) {
            e.printStackTrace();
        } // catch
    } // loadDataSet(String)

    public void analyseSentiment() {
        System.out.println("Analysing tweet sentiment");
        scanner.start();
        sqlThread.start();
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
                    deleteError(id);
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
                        scanner.interrupt();
                        break;
                    } else {
                        deleteError(id);
                        continue;
                    } // else
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

    private void close() {
        if (parseThread != null) {
            try {
                parseThread.join();
            } catch (final InterruptedException e) {
                e.printStackTrace();
            } // catch
            parseThread.removeListener(this);
        } // if
        if (scanner.isAlive()) {
            scanner.interrupt();
            scanner = null;
        } // if
        if (res != null) {
            try {
                res.close();
            } catch (final SQLException e) {
                e.printStackTrace();
            } // catch
        } // if
        if (sqlThread != null) {
            sqlThread.interrupt();
            sqlThread = null;
        } // if
        sa = null;
    } // close()

    @Override
    public void onParseComplete(final long id, final String sentiment) {
        sqlThread.addTask(new SentimentSQLTask(id, sentiment));
    } // onParseComplete(long, String)

    @Override
    public void onParseComplete(final long id, final String sentiment, final String sentimentScore) {
        sqlThread.addTask(new SentimentScoreSQLTask(id, sentiment, sentimentScore));
    } // onParseComplete(long, String, String)

    private void deleteError(final long id) {
        sqlThread.addTask(new DeleteTweetSQLTask(id));
    } // deleteError(long)

} // SentimentAnalysis