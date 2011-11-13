package uk.ac.manchester.cs.patelt9.twitter.stream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;

import javax.net.ssl.HttpsURLConnection;

import sun.misc.BASE64Encoder;
import uk.ac.manchester.cs.patelt9.twitter.data.SQLThread;
import uk.ac.manchester.cs.patelt9.twitter.data.Tweet;
import uk.ac.manchester.cs.patelt9.twitter.data.sqltask.DeleteTweetIdSQLTask;
import uk.ac.manchester.cs.patelt9.twitter.data.sqltask.InsertSQLTask;
import uk.ac.manchester.cs.patelt9.twitter.parse.ScannerThread;
import uk.ac.manchester.cs.patelt9.twitter.parse.StreamParseThread;
import uk.ac.manchester.cs.patelt9.twitter.parse.StreamParseThread.ParseListener;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

public abstract class StreamingApi implements ParseListener {
    private static String userPassword = null, encoding = null;

    private final String urlString;
    private final int counterInterval;
    private final JsonParser jp;

    private HttpsURLConnection con = null;
    private JsonReader jsonReader = null;
    private/* volatile */boolean stillStream = true; // Only changes once so no need to waste time
    private StreamParseThread parseThread = null;
    private ScannerThread scanner = null;
    private SQLThread sqlThread = null;

    @Override
    public void onParseComplete(final Tweet t) {
        addToDb(t);
    } // onParseComplete(Tweet)

    @Override
    public void onParseComplete(final long id) {
        sqlThread.addTask(new DeleteTweetIdSQLTask(id));
    } // onParseComplete(long)

    static {
        getUserPass();
    } // static

    protected SQLThread getSqlThread() {
        return sqlThread;
    } // getSqlThread()

    protected HttpsURLConnection getConnection() {
        return con;
    } // getConnection()

    private static void getUserPass() {
        BufferedReader userPass = null;
        try {
            userPass = new BufferedReader(new FileReader(new File("userpass.txt")));
            userPassword = userPass.readLine();
            encoding = new BASE64Encoder().encode(userPassword.getBytes());
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
            System.err.println("Login file not found");
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            if (userPass != null) {
                try {
                    userPass.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                } // catch
            } // if
        } // finally
    } // getUserPass()

    protected StreamingApi(final String url, final int interval) throws SQLException {
        counterInterval = interval;
        urlString = url;
        jp = new JsonParser();
        sqlThread = new SQLThread();
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
        if (sqlThread != null) {
            sqlThread.interrupt();
            sqlThread = null;
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

    public StreamParseThread streamTweet() {
        return onJsonReadComplete(jp.parse(jsonReader).getAsJsonObject());
    } // streamTweet()

    public void streamTweets() {
        try {
            initialiseReader();
        } catch (final IOException e) {
            e.printStackTrace();
            return;
        } // catch

        scanner.start();
        sqlThread.start();

        System.out.println("Started");
        for (int i = 1; stillStream; i++) {
            if (i % counterInterval == 0) {
                System.out.println(i);
            } else if (i == Integer.MAX_VALUE) {
                i = 1;
            } // else if
            streamTweet();
        } // for

        close();
    } // streamTweets()

    private StreamParseThread onJsonReadComplete(final JsonObject jo) {
        parseThread = new StreamParseThread(jo);
        parseThread.addListener(this);
        parseThread.start();
        return parseThread;
    } // onJsonReadComplete(JsonObject)

    protected boolean addToDb(final Tweet t) {
        return sqlThread.addTask(new InsertSQLTask(t));
    } // addToDb(Tweet)
} // StreamingApi
