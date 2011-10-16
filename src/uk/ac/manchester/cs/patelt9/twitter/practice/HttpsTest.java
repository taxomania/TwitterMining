package uk.ac.manchester.cs.patelt9.twitter.practice;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import sun.misc.BASE64Encoder;
import uk.ac.manchester.cs.patelt9.twitter.TwitterUser;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

public class HttpsTest {
    // URL for Twitter Streaming API sample; 1% of all tweets
    private static final String TWITTER_STREAM_API = "https://stream.twitter.com/1/statuses/sample.json";

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
        new HttpsTest().test();
    } // main(String[])

    private volatile Map<Long, TwitterUser> tweeters = new HashMap<Long, TwitterUser>();

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
            BufferedReader userPass = null;
            try {
                userPass = new BufferedReader(new FileReader(new File("userpass.txt")));
                final String userPassword = userPass.readLine();
                final String encoding = new BASE64Encoder().encode(userPassword.getBytes());
                con.setRequestProperty("Authorization", "Basic " + encoding);
                con.connect();
                final BufferedReader br = new BufferedReader(new InputStreamReader(
                        con.getInputStream()));
                new Thread() {
                    @Override
                    public void run() {
                        final JsonParser jp = new JsonParser();
                        while (true) {
                            final JsonElement je = jp.parse(new JsonReader(br));
                            // System.out.println(je.toString()); // To see format of JSON response
                            if (je.toString().contains("{\"delete\":")) {
                                // System.out.println("Delete tweet");
                                // TODO: Add in something to delete tweets
                                continue;
                            } // if
                            else if (je.isJsonObject()) {
                                final JsonObject jo = je.getAsJsonObject();
                                // System.out.println(jo.getAsJsonObject("user").toString());
                                final Long id = jo.getAsJsonObject("user")
                                        .getAsJsonPrimitive("id_str").getAsLong();
                                final String tweet = jo.getAsJsonPrimitive("text").getAsString();
                                // System.out.println(Long.toString(id) + ": " + tweet); // Testing

                                final TwitterUser tweeter;
                                if (tweeters.containsKey(id)) {
                                    tweeter = tweeters.get(id);
                                } else {
                                    tweeter = new TwitterUser(id);
                                    tweeters.put(id, tweeter);
                                } // else
                                tweeter.addTweet(tweet);
                                System.out.println(tweeters.get(id).toString()); // Testing maps

                                // TODO: Add into database
                            } // if
                        } // while

                    } // run()
                }.run();
            } catch (final FileNotFoundException e) {
                e.printStackTrace();
                System.err.println("Login file not found");
            } catch (final IOException e) {
                e.printStackTrace();
            } finally {
                if (userPass != null) {
                    userPass.close();
                } // if
            } // finally
        } catch (final IOException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
        } finally { // Unsure how this works with the threads
            if (con != null) {
                con.disconnect();
            } // if
        } // finally

    } // test()
} // HttpsTest
