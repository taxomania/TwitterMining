package uk.ac.manchester.cs.patelt9.twitter.stream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.SQLException;

import javax.net.ssl.HttpsURLConnection;

import sun.misc.BASE64Encoder;
import uk.ac.manchester.cs.patelt9.twitter.StaticFunctions;
import uk.ac.manchester.cs.patelt9.twitter.data.DatabaseThread;
import uk.ac.manchester.cs.patelt9.twitter.data.SQLThread;
import uk.ac.manchester.cs.patelt9.twitter.data.Tweet;
import uk.ac.manchester.cs.patelt9.twitter.data.db.task.DeleteTask;
import uk.ac.manchester.cs.patelt9.twitter.data.db.task.InsertTask;
import uk.ac.manchester.cs.patelt9.twitter.parse.ScannerThread;
import uk.ac.manchester.cs.patelt9.twitter.parse.StreamParseThread;
import uk.ac.manchester.cs.patelt9.twitter.parse.StreamParseThread.ParseListener;

import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.mongodb.MongoException;

public abstract class StreamingApi implements ParseListener {
    private static String encoding = null;

    private final String urlString;
    private final int counterInterval;
    private final JsonParser jp;

    private HttpsURLConnection con = null;
    private JsonReader jsonReader = null;
    private/* volatile */boolean stillStream = true; // Only changes once so no need to waste time
    private StreamParseThread parseThread = null;
    private ScannerThread scanner = null;
    private DatabaseThread dbThread = null;

    protected InsertTask createInsertTask(final Tweet t) {
        return new InsertTask(t);
    } // createInsertTask(Tweet)

    @Override
    public void onParseComplete(final Tweet t) {
        dbThread.addTask(createInsertTask(t));
    } // onParseComplete(Tweet)

    @Override
    public void onParseComplete(final long id) {
        dbThread.addTask(new DeleteTask(id));
    } // onParseComplete(long)

    static {
        encoding = new BASE64Encoder()
                .encode(StaticFunctions.getDetails("userpass.txt").getBytes());
    } // static

    protected HttpsURLConnection getConnection() {
        return con;
    } // getConnection()

    protected StreamingApi(final String url, final int interval) throws MongoException,
            UnknownHostException, SQLException {
        counterInterval = interval;
        urlString = url;
        jp = new JsonParser();
        dbThread = new SQLThread();
        parseThread = new StreamParseThread();
        parseThread.addListener(this);
        scanner = new ScannerThread() {
            protected void performTask() {
                stillStream = false;
            } // performTask()
        };
    } // StreamingApi(String)

    // Public interface for setting up connection
    public void connect() throws IOException, MalformedURLException {
        connect(urlString);
    } // connect()

    private void connect(final String s) throws IOException, MalformedURLException {
        connect(new URL(s));
    } // connect(String)

    private void connect(final URL url) throws IOException {
        System.out.println("Connecting to " + url.toString());
        con = (HttpsURLConnection) url.openConnection();
        con.setRequestProperty("Authorization", "Basic " + encoding);
        connect(con);
    } // connect(URL)

    // This is to be overridden by subclasses implementing HTTPS POST
    protected void connect(final HttpsURLConnection con) throws IOException {
        con.connect();
    } // connect(HttpsURLConnection)

    private void close() {
        if (parseThread != null) {
            parseThread.interrupt();
            parseThread.removeListener(this);
        } // if
        if (scanner.isAlive()) {
            scanner.interrupt();
            scanner = null;
        } // if
        if (dbThread != null) {
            dbThread.interrupt();
            dbThread = null;
        } // if
        disconnect();
    } // close()

    private void disconnect() {
        if (jsonReader != null) {
            try {
                jsonReader.close();
            } catch (final IOException e) {
                e.printStackTrace();
            } // catch
        } // if
        if (con != null) {
            con.disconnect();
        } // if
    } // disconnect()

    public void initialiseReader() throws IOException {
        jsonReader = new JsonReader(new BufferedReader(new InputStreamReader(con.getInputStream())));
    } // initialiseReader()

    public void streamTweets() {
        try {
            initialiseReader();
        } catch (final IOException e) {
            e.printStackTrace();
            return;
        } // catch

        scanner.start();
        dbThread.start();
        parseThread.start();

        System.out.println("Started");
        for (int i = 1; stillStream; i++) {
            if (i % counterInterval == 0) {
                System.out.println(i);
            } else if (i == Integer.MAX_VALUE) {
                i = 1;
            } // else if
            parseThread.addTask(jp.parse(jsonReader).getAsJsonObject());
        } // for

        close();
    } // streamTweets()
} // StreamingApi
