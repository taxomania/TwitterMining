package uk.ac.manchester.cs.patelt9.twitter.stream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

import quicktime.app.actions.NotifyListener;

import sun.misc.BASE64Encoder;
import uk.ac.manchester.cs.patelt9.twitter.JsonObjectThrowable;
import uk.ac.manchester.cs.patelt9.twitter.ParseListener;
import uk.ac.manchester.cs.patelt9.twitter.Tweet;
import uk.ac.manchester.cs.patelt9.twitter.data.SqlConnector;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

public abstract class StreamingApi {
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

    // All streams closed internally
    private void close() {
        System.out.println(Integer.toString(count) + " tweets added");
        disconnect();
        if (stdInScanner != null) {
            stdInScanner.close();
        } // if
        if (sql != null) {
           // sql.close();
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

    private final class ExceptionHandler implements UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            if (e instanceof TweetThrowable) {
                System.out.println(((TweetThrowable) e).getTweet().getId());
            } else if (e instanceof DeleteThrowable) {
                System.out.println(((DeleteThrowable) e).getId());
            }
        } // uncaughtException(Thread, Throwable)
    } // ExceptionHandler

    private final class TweetParseListener implements ParseListener {

        @Override
        public void onParseComplete(final Tweet t) {
            count += addToDb(t.getId(), t.getScreenName(), t.getTweet(), t.getCreatedAt(),
                    t.getUserId());
        } // onParseComplete(Tweet)

        @Override
        public void onParseComplete(final long id) {
            count -= sql.deleteTweet(id);
        } // onParseComplete(long)
    } // TweetParseListener

    private class ParseThread extends Thread {
        private final JsonObject jo;

        public ParseThread(final JsonObject jo) {
            this("Parse", jo);
        } // ParseThread()

        public ParseThread(final String s, final JsonObject jo) {
            super(s);
            this.jo = jo;
        } // ParseThread(String)

        @Override
        public void run() {
            // throwJsonObject(jo);
            if (isTweetJsonObject(jo)) {
                notifyListeners(getTweet(jo));
            } else {
                notifyListeners(getDeleteStatusId(jo));
            } // else
              // count += parseJsonObject(jo);
        } // run()

        Set<ParseListener> listeners = new HashSet<ParseListener>();

        public void notifyListeners(final Tweet t) {
            for (final ParseListener listener : listeners) {
                listener.onParseComplete(t);
            } // for
        } // notifyListeners(Tweet)

        public void notifyListeners(final long id) {
            for (final ParseListener listener : listeners) {
                listener.onParseComplete(id);
            } // for
        } // notifyListeners(long)

        public void addListener(final ParseListener listener) {
            listeners.add(listener);
        } // addListener(ParseListener)
    } // ParseThread

    private class StreamThread extends Thread {
        public StreamThread() {
            this("Stream");
        } // StreamThread()

        public StreamThread(final String s) {
            super(s);
        } // StreamThread(String)

        @Override
        public void run() {
            System.out.println("Started");
            final JsonParser jp = new JsonParser();
            for (int i = 1; !isInterrupted(); i++) {
                if (i % counterInterval == 0) {
                    System.out.println(i);
                } else if (i == Integer.MAX_VALUE) {
                    i = 1;
                } // else if
                final JsonObject jo = jp.parse(jsonReader).getAsJsonObject();
                final ParseThread parse = new ParseThread(jo);
                parse.addListener(new TweetParseListener());
                parse.setUncaughtExceptionHandler(new ExceptionHandler());
                parse.start();
            } // while
        } // run()
    } // StreamThread

    private StreamThread t;

    private final class TweetThrowable extends JsonObjectThrowable {
        private final Tweet tweet;

        public TweetThrowable(final Tweet t) {
            tweet = t;
        } // TweetThrowable(Tweet)

        public Tweet getTweet() {
            return tweet;
        } // getTweet()
    } // TweetThrowable

    private final class DeleteThrowable extends JsonObjectThrowable {
        private final long id;

        public DeleteThrowable(final long id) {
            this.id = id;
        } // DeleteThrowable(long)

        public long getId() {
            return id;
        } // getId()
    } // DeleteThrowable

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
                if (stdInScanner.nextLine().contains("exit")) {
                    t.interrupt();
                } // if
            } // run()
        }.start();

        t = new StreamThread();
        t.start();

        // Hold loop until thread has closed, this also needs to allow for parser thread
        try {
            t.join();
        } catch (final InterruptedException e) {
            e.printStackTrace();
        } // catch
        close();
    } // streamTweets()

    private boolean isTweetJsonObject(final JsonObject jo) {
        if (jo.toString().contains("{\"delete\":")) {
            return false;
        } else {
            return true;
        } // else
    } // isTweetJsonObject(JsonObject)

    private int parseTweetJsonObject(final JsonObject jo) {
        final JsonObject user = jo.getAsJsonObject("user");
        try { // This is only being used to find a rare bug
            final Long userId = user.getAsJsonPrimitive("id_str").getAsLong();
            final String screenName = user.getAsJsonPrimitive("screen_name").getAsString();
            final String tweet = jo.getAsJsonPrimitive("text").getAsString();
            final Long tweetId = jo.getAsJsonPrimitive("id_str").getAsLong();
            final String createdAt = parseCreatedAtForSql(jo.getAsJsonPrimitive("created_at")
                    .getAsString());
            return addToDb(tweetId, screenName, tweet, createdAt, userId);
        } catch (final NullPointerException e) {
            e.printStackTrace();
            System.err.println(jo.toString());
            System.err.println(user.toString());
            return 0;
        } // catch
    } // parseTweetJsonObject(JsonObject)

    private void throwTweet(final JsonObject jo) throws TweetThrowable {
        throw new TweetThrowable(getTweet(jo));
    } // throwTweet(JsonObject)

    // Want to use this to enable callback to main thread for sql tasks
    private Tweet getTweet(final JsonObject jo) {
        final JsonObject user = jo.getAsJsonObject("user");
        final Long userId = user.getAsJsonPrimitive("id_str").getAsLong();
        final String screenName = user.getAsJsonPrimitive("screen_name").getAsString();
        final String tweet = jo.getAsJsonPrimitive("text").getAsString();
        final Long tweetId = jo.getAsJsonPrimitive("id_str").getAsLong();
        final String createdAt = parseCreatedAtForSql(jo.getAsJsonPrimitive("created_at")
                .getAsString());
        return new Tweet(tweetId, userId, screenName, tweet, createdAt);
    } // getTweet(JsonObject)

    private int parseDeleteJsonObject(final JsonObject jo) {
        return sql.deleteTweet(getDeleteStatusId(jo)) * -1;
    } // parseDeleteJsonObject(JsonObject)

    private void throwDeleteStatusId(final JsonObject jo) throws DeleteThrowable {
        throw new DeleteThrowable(getDeleteStatusId(jo));
    } // throwDeleteStatusId(JsonObject)

    // Want to use this to enable callback to main thread for sql tasks
    private long getDeleteStatusId(final JsonObject jo) {
        return jo.getAsJsonObject("delete").getAsJsonObject("status").getAsJsonPrimitive("id_str")
                .getAsLong();
    } // getDeleteStatusId(JsonObject)

    private void throwJsonObject(final JsonObject jo) throws JsonObjectThrowable {
        if (isTweetJsonObject(jo)) {
            throwTweet(jo);
        } else {
            throwDeleteStatusId(jo);
        } // else
    } // throwJsonObject(JsonObject)

    private int parseJsonObject(final JsonObject jo) {
        if (isTweetJsonObject(jo)) {
            return parseTweetJsonObject(jo);
        } else {
            return parseDeleteJsonObject(jo);
        } // else if
    } // parseJsonObject(JsonObject)

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
