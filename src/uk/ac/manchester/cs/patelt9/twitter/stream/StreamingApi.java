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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

import sun.misc.BASE64Encoder;
import uk.ac.manchester.cs.patelt9.twitter.data.SqlConnector;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

public abstract class StreamingApi {
    // To avoid filling JVM heap - This is only used while parsing is done sequentially
    private static final int MAX_TWEETS = 5000;

    private static String userPassword = null, encoding = null;

    private int count = 0;
    private final String urlString;
    private final int counterInterval;
    private HttpsURLConnection con = null;
    private volatile Scanner stdInScanner = null;
    private volatile SqlConnector sql = null;
    private volatile JsonReader jsonReader = null;

    static {
        getUserPass();
    } // static

    protected SqlConnector getSqlConnector() {
        return sql;
    } // getSqlConnector()

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
        sql = SqlConnector.getInstance();
        counterInterval = interval;
        urlString = url;
        stdInScanner = new Scanner(System.in);
    } // StreamingApi(String)

    // Public interface for setting up connection
    public void connect() throws IOException, MalformedURLException {
        connect(urlString);
    } // connect()

    private void connect(final String s) throws IOException, MalformedURLException {
        final URL url = new URL(s);
        connect(url);
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

    public void close() {
        System.out.println(Integer.toString(count) + " tweets added");
        disconnect();
        if (stdInScanner != null) {
            stdInScanner.close();
        } // if
        if (sql != null) {
            sql.close();
        } // if
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

    private class StreamThread extends Thread {
        public StreamThread() {
            this("Stream");
        } // StreamThread()

        public StreamThread(final String s) {
            super(s);
        } // StreamThread(String)

        @Override
        public void run() {
            final JsonParser jp = new JsonParser();
            // int i = 0;
            // while (!isInterrupted()) { // Eventually use this
            for (int i = 0; i < MAX_TWEETS; i++) { // Testing purposes
                if (isInterrupted()) break;
                if (i % counterInterval == 0) System.out.println(i); // counter
                jsonElements.addLast(jp.parse(jsonReader));
                // i++;
            } // while
        } // run()
    } // StreamThread

    private StreamThread t;
    private volatile boolean stillStream = true;

    public void streamTweets() {
        try {
            jsonReader = new JsonReader(new BufferedReader(new InputStreamReader(
                    con.getInputStream())));
        } catch (final IOException e) {
            e.printStackTrace();
            return;
        } // catch

        new Thread("Scanner") {
            public void run() {
                while (stillStream) {
                    String s = stdInScanner.nextLine();
                    if (s.contains("stop")) {
                        t.interrupt();
                    } else if (s.contains("exit")) {
                        t.interrupt();
                        stillStream = false;
                    } // else
                } // while
            } // run()
        }.start();

        // Eventually want to parse elements in separate thread
        // new Thread() {
        // public void run() {
        // parseJsonElements();
        // }
        // }.start();

        // Continuously stream
        while (stillStream) {
            t = new StreamThread();
            t.start();

            // Hold loop until thread has closed, this also needs to allow for parser thread
            while (true) {
                if (!t.isAlive()) {
                    break;
                } // if
            } // while

            System.out.println("Parsing");
            parseJsonElements();
        } // while
    } // streamTweets()

    private LinkedList<JsonElement> jsonElements = new LinkedList<JsonElement>();

    private void parseJsonElements() {
        while (!jsonElements.isEmpty()) {
            final JsonElement je;
            try {
                je = jsonElements.removeFirst();
            } catch (final NoSuchElementException e) {
                continue;
            } // catch

            if (je.isJsonObject()) {
                final JsonObject jo = je.getAsJsonObject();
                if (je.toString().contains("{\"delete\":")) {
                    final Long tweetId = jo.getAsJsonObject("delete").getAsJsonObject("status")
                            .getAsJsonPrimitive("id_str").getAsLong();
                    count -= sql.deleteTweet(tweetId);
                } else {
                    final JsonObject user = jo.getAsJsonObject("user");
                    final Long userId = user.getAsJsonPrimitive("id_str").getAsLong();
                    final String screenName = user.getAsJsonPrimitive("screen_name").getAsString();
                    final String tweet = jo.getAsJsonPrimitive("text").getAsString();
                    final Long tweetId = jo.getAsJsonPrimitive("id_str").getAsLong();
                    final String createdAt = parseCreatedAtForSql(jo.getAsJsonPrimitive(
                            "created_at").getAsString());
                    count += addToDb(tweetId, screenName, tweet, createdAt, userId);
                } // else if
            } // if
        } // while
    } // parseJsonElements()

    protected int addToDb(final Long tweetId, final String screenName, final String tweet,
            final String createdAt, final Long userId) {
        return sql.insertTweet(tweetId, screenName, tweet, createdAt, userId);
    } // addToDb(Long, String, String, String, Long)

    protected static String parseCreatedAtForSql(final String date) {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy");
        dateFormat.setLenient(false);
        final SimpleDateFormat sqlFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return sqlFormat.format(dateFormat.parse(date));
        } catch (final ParseException e) {
            e.printStackTrace();
            return null;
        } // catch
    } // parseCreatedAtForSql(String)
} // StreamingApi