package uk.ac.manchester.cs.patelt9.twitter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

import sun.misc.BASE64Encoder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

public class StreamingApi {
    // URL for Twitter Streaming API sample; 1% of all tweets
    private static final String TWITTER_STREAM_API = "https://stream.twitter.com/1/statuses/sample.json";
    // To avoid filling JVM heap - This is only used while parsing is done sequentially
    private static final int MAX_TWEETS = 5000;
    private static final int TWEET_COUNTER_INTERVAL = 200;

    private static String userPassword = null, encoding = null;
    private static StreamingApi stream = null;

    private HttpsURLConnection con = null;
    private volatile Scanner stdInScanner = null;
    private volatile SqlConnector sql = null;
    private volatile JsonReader jsonReader = null;

    static {
        getUserPass();
    } // static

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

    // Singleton lock as you cannot have more than one connection
    public static StreamingApi getInstance() {
        if (stream == null) {
            stream = new StreamingApi();
        } // if
        return stream;
    } // getInstance()

    private StreamingApi() {
        new Thread() {
            @Override
            public void run() {
                sql = SqlConnector.getInstance();
            } // run()
        }.start();
        connect();
        stdInScanner = new Scanner(System.in);
    } // StreamingApi()

    private void connect() {
        final URL url;
        try {
            url = new URL(TWITTER_STREAM_API);
            try {
                con = (HttpsURLConnection) url.openConnection();
                try {
                    con.setRequestProperty("Authorization", "Basic " + encoding);
                    con.connect();
                } catch (final IOException e) {
                    e.printStackTrace();
                } // catch
            } catch (final IOException e) {
                e.printStackTrace();
            } // catch
        } catch (final MalformedURLException e) {
            e.printStackTrace();
            System.err.println("Error parsing URL");
        } // catch
    } // connect()

    public void close() {
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
                if (i % TWEET_COUNTER_INTERVAL == 0) System.out.println(i); // counter
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
        int counter = 0;
        while (!jsonElements.isEmpty()) {
            final JsonElement je;
            try {
                je = jsonElements.removeFirst();
            } catch (final NoSuchElementException e) {
                continue;
            } // catch

            if (je.toString().contains("{\"delete\":")) {
                // System.out.println("Delete tweet");
                // TODO: Add in something to delete tweets
                continue;
            } else if (je.isJsonObject()) {
                final JsonObject jo = je.getAsJsonObject();
                final Long id = jo.getAsJsonObject("user").getAsJsonPrimitive("id_str").getAsLong();
                final String tweet = jo.getAsJsonPrimitive("text").getAsString();
                final String createdAt = parseCreatedAtForSql(jo.getAsJsonPrimitive("created_at")
                        .getAsString());

                counter += sql.insertTweet(id, tweet, createdAt);
            } // else if
        } // while
        System.out.println(Integer.toString(counter) + " tweets added");
    } // parseJsonElements()

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
