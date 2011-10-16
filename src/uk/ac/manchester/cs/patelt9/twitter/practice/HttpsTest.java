package uk.ac.manchester.cs.patelt9.twitter.practice;

import java.io.BufferedReader;
import java.io.File;
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
    private static final String TWITTER_STREAM_API = "https://stream.twitter.com/1/statuses/sample.json";

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
        try {
            final HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            final BufferedReader userPass = new BufferedReader(new FileReader(new File(
                    "userpass.txt")));
            final String userPassword = userPass.readLine();
            final String encoding = new BASE64Encoder().encode(userPassword.getBytes());
            con.setRequestProperty("Authorization", "Basic " + encoding);
            con.connect();
            final BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            new Thread() {
                @Override
                public void run() {
                    while (true) {
                        final JsonParser jp = new JsonParser();
                        final JsonElement je = jp.parse(new JsonReader(br));
                        if (je.toString().contains("{\"delete\":")) {
                            System.out.println("Delete tweet");
                            continue;
                        } // if
                        else if (je.isJsonObject()) {
                            final JsonObject jo = je.getAsJsonObject();
                            final Long id = jo.getAsJsonObject("user").getAsJsonPrimitive("id_str")
                                    .getAsLong();
                            final String tweet = jo.getAsJsonPrimitive("text").getAsString();
                            // System.out.println(Long.toString(id) + ": " + tweet);

                            final TwitterUser tweeter;
                            if (tweeters.containsKey(id)) {
                                tweeter = tweeters.get(id);
                            } else {
                                tweeter = new TwitterUser(id);
                                tweeters.put(id, tweeter);
                            } // else
                            tweeter.addTweet(tweet);
                            System.out.println(tweeters.get(id).toString());

                        } // if

                    } // while
                }
            }.run();

        } catch (final IOException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
        } // catch
        /*
         * try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); } for
         * (TwitterUser t : tweeters.values()) { System.out.println(t.toString()); }
         */
    } // test()
} // HttpsTest
