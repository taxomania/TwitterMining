package uk.ac.manchester.cs.patelt9.twitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import uk.ac.manchester.cs.patelt9.twitter.data.DatabaseThread;
import uk.ac.manchester.cs.patelt9.twitter.data.SQLThread;
import uk.ac.manchester.cs.patelt9.twitter.data.Tweet;
import uk.ac.manchester.cs.patelt9.twitter.data.TweetVerbose;
import uk.ac.manchester.cs.patelt9.twitter.data.User;
import uk.ac.manchester.cs.patelt9.twitter.data.db.task.InsertKeywordTask;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

public class SearchAPI {
    private static final String BASE_URL = "http://search.twitter.com/search.json?rpp=100&include_entities=false&q=";
    private final String url;
    private final String query;

    public static void main(String[] args) throws SQLException {
        if (args.length < 1) {
            System.out.println("Please provide a search term");
        } else {
            new SearchAPI(args[0]).doAll(); // TEST
        }
    } // main(String[])

    private DatabaseThread dbThread = null;

    public SearchAPI(final String q) throws SQLException {
        query = q;
        url = BASE_URL + query;
        dbThread = new SQLThread(false);
        dbThread.start();
    } // SearchAPI(String)

    private HttpResponse performSearch() {
        final HttpClient client = new DefaultHttpClient();
        try {
            final HttpGet getMethod = new HttpGet(url);
            try {
                return client.execute(getMethod);
            } catch (final IOException e) {
                e.printStackTrace();
            } // catch
        } catch (final IllegalArgumentException e) {
            System.out.println("Could not parse URL");
        } // catch
        return null;
    } // performSearch()

    public void doAll() {
        final List<Tweet> tweets = parseResponse(performSearch());
        for (final Tweet t : tweets){
            System.out.println(t.toString());
        }
        close();
    } // doAll()

    public void close() {
        if (dbThread != null) {
            dbThread.interrupt();
            dbThread = null;
        } // if
    } // close()

    private List<Tweet> parseResponse(final HttpResponse response) {
        if (response != null) {
            JsonReader jr = null;
            try {
                jr = new JsonReader(new BufferedReader(new InputStreamReader(response.getEntity()
                        .getContent())));
                final JsonParser jp = new JsonParser();
                final JsonObject jo = jp.parse(jr).getAsJsonObject();
                return parseJsonResponse(jo);
            } catch (final IllegalStateException e) {
                e.printStackTrace();
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
        } // if
        return null;
    } // parseResponse(HttpResponse)

    private List<Tweet> parseJsonResponse(final JsonObject jo) {
        return parseJson(jo.getAsJsonArray("results"));
    } // parseJsonResponse(JsonObject)

    private List<Tweet> parseJson(final JsonArray ja) {
        final List<Tweet> list = new ArrayList<Tweet>();
        for (final JsonElement je : ja) {
            final Tweet t = parseJson(je.getAsJsonObject());
            //dbThread.addTask(new InsertKeywordTask(t, query));
            list.add(t);
        } // for
        return list;
    } // parseJson(JsonArray)

    private Tweet parseJson(final JsonObject jo) {
        final String createdAt = parseCreatedAtForSql(jo.getAsJsonPrimitive("created_at")
                .getAsString());

        final String username = jo.getAsJsonPrimitive("from_user").getAsString();
        final long userId = jo.getAsJsonPrimitive("from_user_id").getAsLong();
        final User user = new User(userId, username);

        final String tweet = jo.getAsJsonPrimitive("text").getAsString();
        final long tweetId = jo.getAsJsonPrimitive("id_str").getAsLong();

        return new TweetVerbose(tweetId, tweet, createdAt, user);
    } // parseJson(JsonObject)

    protected static String parseCreatedAtForSql(final String date) {
        // Date format "Mon, 14 Nov 2011 19:27:37 +0000"
        final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss ZZZZZ");
        dateFormat.setLenient(false);
        final SimpleDateFormat sqlFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return sqlFormat.format(dateFormat.parse(date));
        } catch (final ParseException e) {
            e.printStackTrace();
            return null;
        } // catch
    } // parseCreatedAtForSql(String)
} // SearchAPI
