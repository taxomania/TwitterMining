package uk.ac.manchester.cs.patelt9.twitter.practice;

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

import javax.net.ssl.HttpsURLConnection;

import sun.misc.BASE64Encoder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

public class HttpsPractice {
    // URL for Twitter Streaming API sample; 1% of all tweets
    private static final String TWITTER_STREAM_API = "https://stream.twitter.com/1/statuses/sample.json";
    private static String userPassword = null, encoding = null;

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

    private volatile LinkedList<JsonElement> elementQueue = new LinkedList<JsonElement>();

    private static void printLineBreak() {
        System.out.println("-----------------------");
    } // printLineBreak()

    @SuppressWarnings("unused")
    private static void printHeaders() {
        printLineBreak();
        System.out.println("START");
        printLineBreak();
    } // printHeaders()

    public static void main(final String[] args) {
        new HttpsPractice().test();
    } // main(String[])

    // private final Map<Long, TwitterUser> tweeters = new HashMap<Long, TwitterUser>();

    private void test() {
        final URL url;
        try {
            url = new URL(TWITTER_STREAM_API);
        } catch (final MalformedURLException e) {
            e.printStackTrace();
            System.err.println("Error parsing URL");
            return;
        } // catch
        HttpsURLConnection con = null;
        try {
            con = (HttpsURLConnection) url.openConnection();
            try {
                con.setRequestProperty("Authorization", "Basic " + encoding);
                con.connect();
                final BufferedReader br = new BufferedReader(new InputStreamReader(
                        con.getInputStream()));
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        final JsonParser jp = new JsonParser();
                        for (int i = 0; i < 10; i++) {
                            elementQueue.addLast(jp.parse(new JsonReader(br)));
                        }
                    }
                };
                /*
                 * final Thread t = new Thread("RETRIEVE") {
                 *
                 * @Override public void run() { final JsonParser jp = new JsonParser(); // while
                 * (true) { for (int i = 0; i < 100; i++) { queue.add(jp.parse(new JsonReader(br)));
                 *
                 * // final JsonElement je = jp.parse(new JsonReader(br)); /*
                 *
                 * // System.out.println(je.toString()); // To see format of JSON response if
                 * (je.toString().contains("{\"delete\":")) { // System.out.println("Delete tweet");
                 * // TODO: Add in something to delete tweets continue; } // if else if
                 * (je.isJsonObject()) { final JsonObject jo = je.getAsJsonObject(); //
                 * System.out.println(jo.getAsJsonObject("user").toString()); final Long id =
                 * jo.getAsJsonObject("user") .getAsJsonPrimitive("id_str").getAsLong(); final
                 * String tweet = jo.getAsJsonPrimitive("text").getAsString(); //
                 * System.out.println(Long.toString(id) + ": " + tweet); // Testing
                 *
                 * final TwitterUser tweeter; if (tweeters.containsKey(id)) { tweeter =
                 * tweeters.get(id); } else { tweeter = new TwitterUser(id); tweeters.put(id,
                 * tweeter); } // else tweeter.addTweet(tweet); //
                 * System.out.println(tweeters.get(id).toString()); // Testing maps
                 *
                 * // TODO: Add into database } // if
                 */// } // while
                /*
                 * } // run() }; t.run();
                 */

                while (true) {
                    new Thread(r, "RETRIEVE").run();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } // catch

                    while (!elementQueue.isEmpty()) {
                        final JsonElement je = elementQueue.removeFirst();

                        // System.out.println(je.toString()); // To see format of JSON response
                        if (je.toString().contains("{\"delete\":")) {
                            // System.out.println("Delete tweet");
                            // TODO: Add in something to delete tweets
                            continue;
                        } // if
                        else if (je.isJsonObject()) {
                            final JsonObject jo = je.getAsJsonObject();
                            // System.out.println(jo.toString());
                            System.out.println(jo.getAsJsonObject("user").toString());
                            // final Long id =
                            // jo.getAsJsonObject("user").getAsJsonPrimitive("id_str")
                            // .getAsLong();
                            // final String tweet = jo.getAsJsonPrimitive("text").getAsString();
                            // System.out.println(Long.toString(id) + ": " + tweet); // Testing
                            final String createdAt = parseCreatedAtForSql(jo.getAsJsonPrimitive(
                                    "created_at").getAsString());
                            System.out.println(createdAt);
                            /*
                             * final TwitterUser tweeter; if (tweeters.containsKey(id)) { tweeter =
                             * tweeters.get(id); } else { tweeter = new TwitterUser(id);
                             * tweeters.put(id, tweeter); } // else tweeter.addTweet(new
                             * Tweet(tweet, createdAt)); //
                             * System.out.println(tweeters.get(id).toString()); // Testing maps
                             */
                            // TODO: Add into database
                        } // if
                    } // while
                } // while
            } catch (final IOException e) {
                e.printStackTrace();
            } // catch
        } catch (final IOException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
        } finally { // Unsure how this works with the threads
            if (con != null) {
                con.disconnect();
            } // if
        } // finally

    } // test()

    public static String parseCreatedAtForSql(final String date) {
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
} // HttpsTest
