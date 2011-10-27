package uk.ac.manchester.cs.patelt9.twitter.test;

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

import javax.net.ssl.HttpsURLConnection;

import sun.misc.BASE64Encoder;
import uk.ac.manchester.cs.patelt9.twitter.SqlConnector;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

public class StreamingApi {
    // URL for Twitter Streaming API sample; 1% of all tweets
    private static final String TWITTER_STREAM_API = "https://stream.twitter.com/1/statuses/sample.json";
    private static String userPassword = null, encoding = null;

    private static StreamingApi stream = null;

    private HttpsURLConnection con = null;

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
        connect();
    } // StreamingApi()

    private void connect() {
        final URL url;
        try {
            url = new URL(TWITTER_STREAM_API);
        } catch (final MalformedURLException e) {
            e.printStackTrace();
            System.err.println("Error parsing URL");
            return;
        } // catch
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
    } // connect()

    public void disconnect() {
        if (con != null) {
            con.disconnect();
        } // if
    } // disconnect()

    public void streamTweets() {
        JsonReader jr = null;
        try {
            jr = new JsonReader(new BufferedReader(new InputStreamReader(con.getInputStream())));
            final JsonParser jp = new JsonParser();
            new Thread() {
                public void run() {
                    // parseJsonElements();
                };
            }.run();
            // while (true) {
            for (int i = 0; i < 10; i++) { // Testing purposes
                jsonElements.addLast(jp.parse(jr));
            } // for
            parseJsonElements();
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            if (jr != null) {
                try {
                    jr.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                } // catch
            } // if
        } // finally
    } // streamTweets()

    public LinkedList<JsonElement> getJsonElements() {
        return jsonElements;
    } // getJsonElements()

    private LinkedList<JsonElement> jsonElements = new LinkedList<JsonElement>();

    private void parseJsonElements() {
        final SqlConnector sql = SqlConnector.getInstance();
        // while (true) {
        for (int i = 0; i < 10; i++) { // Testing purposes

            // System.out.println(jsonElements.toString());
            // System.err.println("B");
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
                // System.out.println(jo.toString());

                // System.out.println(jo.getAsJsonObject("user").toString());
                final Long id = jo.getAsJsonObject("user").getAsJsonPrimitive("id_str").getAsLong();
                final String tweet = jo.getAsJsonPrimitive("text").getAsString();
                // System.out.println(Long.toString(id) + ": " + tweet); // Testing
                final String createdAt = parseCreatedAtForSql(jo.getAsJsonPrimitive("created_at")
                        .getAsString());

                sql.insertTweet(id, tweet, createdAt);

                //
                // System.out.println(createdAt);
                // final TwitterUser tweeter;
                // if (tweeters.containsKey(id)) {
                // tweeter = tweeters.get(id);
                // } else {
                // tweeter = new TwitterUser(id);
                // tweeters.put(id, tweeter);
                // } // else
                // tweeter.addTweet(new Tweet(tweet, createdAt));
                // System.out.println(tweeters.get(id).toString()); // Testing maps

                // TODO: Add into database
            } // else if
            // System.out.println(jsonElements.toString());
        } // while
        sql.close();
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
