package uk.ac.manchester.cs.patelt9.twitter;

import java.io.IOException;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import uk.ac.manchester.cs.patelt9.twitter.data.DatabaseThread;
import uk.ac.manchester.cs.patelt9.twitter.data.MongoThread;
import uk.ac.manchester.cs.patelt9.twitter.data.SQLThread;
import uk.ac.manchester.cs.patelt9.twitter.data.db.MongoConnector;
import uk.ac.manchester.cs.patelt9.twitter.data.db.TweetSQLConnector;
import uk.ac.manchester.cs.patelt9.twitter.data.db.task.DeleteTask;
import uk.ac.manchester.cs.patelt9.twitter.data.db.task.SentimentScoreTask;
import uk.ac.manchester.cs.patelt9.twitter.data.db.task.SentimentTask;
import uk.ac.manchester.cs.patelt9.twitter.parse.ScannerThread;
import uk.ac.manchester.cs.patelt9.twitter.parse.SentimentObject;
import uk.ac.manchester.cs.patelt9.twitter.parse.SentimentParseThread;
import uk.ac.manchester.cs.patelt9.twitter.parse.SentimentParseThread.ParseListener;

import com.alchemyapi.api.AlchemyAPI;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

// THIS CLASS STILL ONLY WORKS PROPERLY WITH MYSQL
public class SentimentAnalysis implements ParseListener {
    public static void main(final String[] args) {
        final SentimentAnalysis sa;
        try {
            sa = SentimentAnalysis.getInstance();
            sa.analyseSentiment();
        } catch (final IOException e) {
            System.err.println("Could not load API key");
            System.exit(1);
        } catch (final SQLException e) {
            System.err.println("Could not connect to database");
            System.exit(1);
        } // catch
    } // main(String[])

    // @formatter:off
    private static final String DEFAULT_QUERY = "SELECT text, tweet_id FROM tweet WHERE sentiment IS NULL"
            + " AND keyword"
            + " IS NOT NULL"
            // + " = 'app,program,software,windows,osx,mac'"
            + " LIMIT 30000;";
            // + " AND tweet_id = 138048869840859136;";
    // @formatter:on

    private final AlchemyAPI api;

    private ResultSet res;
    private boolean stillAnalyse = true;
    private ScannerThread scanner = null;
    private SentimentParseThread parseThread = null;
    private DatabaseThread dbThread = null;

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
        dbThread = new SQLThread();
        parseThread = new SentimentParseThread();
        parseThread.addListener(this);
    } // SentimentAnalysis()

    private void setRes(final ResultSet res) {
        this.res = res;
    } // setRes(ResultSet)

    public DBCursor getDataSet() throws UnknownHostException, MongoException {
        return MongoConnector.getInstance().loadSentimentDataSet();
    } // getDataSet()

    public void loadDataSet() {
        loadDataSet(DEFAULT_QUERY);
    } // loadDataSet()

    public void loadDataSet(final String sqlStatement) {
        try {
            setRes(TweetSQLConnector.getInstance().executeQuery(sqlStatement));
        } catch (final SQLException e) {
            e.printStackTrace();
        } // catch
    } // loadDataSet(String)

    private void getSentiment(final long id, final String tweet) throws IllegalArgumentException,
            SAXException, XPathExpressionException, ParserConfigurationException, IOException {
        final Document doc = api.TextGetTextSentiment(tweet);
        parseThread.addTask(new SentimentObject(id, doc));
    } // getSentiment(long, String)

    public void analyseSentiment() {
        System.out.println("Analysing tweet sentiment");
        scanner.start();
        dbThread.start();
        parseThread.start();
        try {
            if (dbThread instanceof MongoThread) {
                final DBCursor c = getDataSet();
                while (stillAnalyse && c.hasNext()) {
                    final DBObject ob = c.next();
                    final long id = ((Long) ob.get("tweet_id")).longValue();
                    final String tweet = (String) ob.get("text");
                    try {
                        getSentiment(id, tweet);
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
                } // while
            } else {
                loadDataSet();
                res.beforeFirst();
                while (stillAnalyse && res.next()) {
                    final String tweet = res.getString(1);
                    final Long id = res.getLong(2);
                    try {
                        getSentiment(id, tweet);
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
                } // while
            } // else
            close();
        } catch (final SQLException e) {
            e.printStackTrace();
        } catch (final UnknownHostException e) {
            e.printStackTrace();
        } catch (final MongoException e) {
            e.printStackTrace();
        } // catch
    } // analyseSentiment()

    private void close() {
        if (parseThread != null) {
            parseThread.interrupt();
            parseThread.removeListener(this);
        } // if
        if (scanner != null) {
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
        if (dbThread != null) {
            dbThread.interrupt();
            dbThread = null;
        } // if
        sa = null;
    } // close()

    @Override
    public void onParseComplete(final long id, final String sentiment) {
        dbThread.addTask(new SentimentTask(id, sentiment));
    } // onParseComplete(long, String)

    @Override
    public void onParseComplete(final long id, final String sentiment, final String sentimentScore) {
        dbThread.addTask(new SentimentScoreTask(id, sentiment, sentimentScore));
    } // onParseComplete(long, String, String)

    private void deleteError(final long id) {
        dbThread.addTask(new DeleteTask(id));
    } // deleteError(long)

} // SentimentAnalysis