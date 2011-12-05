package uk.ac.manchester.cs.patelt9.twitter;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;

import uk.ac.manchester.cs.patelt9.twitter.data.DatabaseThread;
import uk.ac.manchester.cs.patelt9.twitter.data.SQLThread;
import uk.ac.manchester.cs.patelt9.twitter.data.db.TweetSQLConnector;
import uk.ac.manchester.cs.patelt9.twitter.data.db.task.SentimentScoreTask;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;

public class BulkSentimentAnalysis {
    private static final String URL = "http://partners-v1.twittersentiment.appspot.com/api/bulkClassifyJson";
    // @formatter:off
    private static final String DEFAULT_QUERY = "SELECT tweet_id, text FROM tweet WHERE sentiment IS NULL"
            + " AND keyword"
            + " IS NOT NULL"
            + " LIMIT 1000;"; // No known limit for api
    // @formatter:on

    private HttpURLConnection con = null;
    private DatabaseThread dbThread = null;

    public static void main(final String[] args) throws IOException, SQLException {
        final BulkSentimentAnalysis b = new BulkSentimentAnalysis();
        b.loadDataSet();
        b.connect(b.createJsonObject());
        b.response();
    } // main(String[])

    private static BulkSentimentAnalysis sa = null;

    public static BulkSentimentAnalysis getInstance() throws IOException, SQLException {
        if (sa == null) {
            sa = new BulkSentimentAnalysis();
        } // if
        return sa;
    } // getInstance()

    private ResultSet res = null;

    public void loadDataSet() {
        loadDataSet(DEFAULT_QUERY);
    } // loadDataSet()

    public void loadDataSet(final String sqlStatement) {
        try {
            res = TweetSQLConnector.getInstance().executeQuery(sqlStatement);
        } catch (final SQLException e) {
            e.printStackTrace();
        } // catch
    } // loadDataSet(String)

    public JsonObject createJsonObject() throws SQLException, UnsupportedEncodingException {
        final JsonArray array = new JsonArray();
        loadDataSet();
        res.beforeFirst();
        while (res.next()) {
            final JsonObject jo = new JsonObject();
            jo.add("id", new JsonPrimitive(res.getLong(1)));
            final String text = URLEncoder.encode(res.getString(2),"UTF-8");
            jo.add("text", new JsonPrimitive(text));
            array.add(jo);
        } // while

        final JsonObject data = new JsonObject();
        data.add("data", array);
        return data;
    } // createJsonObject()

    private BulkSentimentAnalysis() throws IOException, SQLException {
        dbThread = new SQLThread();
    } // BulkSentiment()

    public void response() throws IOException {
        final JsonReader j = new JsonReader(new BufferedReader(new InputStreamReader(
                con.getInputStream())));
        final JsonArray ja = new JsonParser().parse(j).getAsJsonObject().getAsJsonArray("data");
        j.close();
        con.disconnect();
        parse(ja);
    } // response()

    public void parse(final JsonArray ja) {
        dbThread.start();
        for (final JsonElement je : ja) {
            final JsonObject jo = je.getAsJsonObject();
            final long id = jo.getAsJsonPrimitive("id").getAsLong();
            final int polarity = jo.getAsJsonPrimitive("polarity").getAsInt();
            final String sentiment;
            switch (polarity) {
                case 0:
                    sentiment = "negative";
                    break;
                default:
                case 2:
                    sentiment = "neutral";
                    break;
                case 4:
                    sentiment = "positive";
                    break;
            } // switch
            dbThread.addTask(new SentimentScoreTask(id, sentiment, Integer.toString(polarity)));
        } // for
        dbThread.interrupt();
    } // parse(JsonArray)

    public void connect(final JsonObject data) throws IOException, MalformedURLException {
        if (res == null) throw new IOException();
        final URL url = new URL(URL);
        System.out.println("Connecting to " + url.toString());
        con = (HttpURLConnection) url.openConnection();
        con.setDoInput(true);
        con.setDoOutput(true);
        con.setRequestMethod("POST");
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(con.getOutputStream());
            out.writeBytes(data.toString());
        } catch (final IOException e) {
            throw new IOException(e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                } // catch
            } // if
        } // finally
    } // connect(URL)

} // BulkSentiment