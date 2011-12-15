package uk.ac.manchester.cs.patelt9.twitter;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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
    // @formatter:off
    private static final String URL =
            "http://partners-v1.twittersentiment.appspot.com/api/bulkClassifyJson";
    private static final String DEFAULT_QUERY = "SELECT tweet_id, text FROM tweet WHERE sentiment "
            + "IS NULL AND keyword "
            // + "IS NOT NULL "
            + "='latest'"
            + "ORDER BY id DESC LIMIT 1000;"; // 10,000 at a time
    // @formatter:on

    private HttpURLConnection con = null;
    private DatabaseThread dbThread = null;

    public static void main(final String[] args) throws IOException, SQLException {
        BulkSentimentAnalysis.getInstance().run();
    } // main(String[])

    public void run() throws IOException, SQLException {
        dbThread.start();
        while (loadDataSet()) {
            connect(createJsonObject());
            response();
        } // while
        dbThread.interrupt();
    } // run()

    private static BulkSentimentAnalysis sa = null;

    public static BulkSentimentAnalysis getInstance() throws SQLException {
        if (sa == null) {
            sa = new BulkSentimentAnalysis();
        } // if
        return sa;
    } // getInstance()

    private ResultSet res = null;

    private boolean loadDataSet() {
        try {
            res = TweetSQLConnector.getInstance().executeQuery(DEFAULT_QUERY);
            return res.first();
        } catch (final SQLException e) {
            e.printStackTrace();
            return false;
        } // catch
    } // loadDataSet()

    private void checkRes() throws IOException {
        if (res == null) throw new IOException("ResultSet not initialised");
    } // checkRes()

    private JsonObject createJsonObject() throws IOException, SQLException,
            UnsupportedEncodingException {
        checkRes();
        final JsonArray array = new JsonArray();
        res.beforeFirst();
        while (res.next()) {
            final JsonObject jo = new JsonObject();
            jo.add("id", new JsonPrimitive(res.getLong(1)));
            jo.add("text", new JsonPrimitive(res.getString(2)));
            array.add(jo);
        } // while

        final JsonObject data = new JsonObject();
        data.add("data", array);
        return data;
    } // createJsonObject()

    private BulkSentimentAnalysis() throws SQLException {
        dbThread = new SQLThread();
    } // BulkSentiment()

    private void response() {
        print("Getting server response");
        JsonReader j = null;
        try {
            j = new JsonReader(new BufferedReader(new InputStreamReader(con.getInputStream())));
        } catch (final IOException e) {
            con.disconnect();
            e.printStackTrace();
            return;
        } // catch
        print("Parsing server response");
        final JsonArray ja = new JsonParser().parse(j).getAsJsonObject().getAsJsonArray("data");
        try {
            j.close();
        } catch (final IOException e) {
            e.printStackTrace();
        } // catch
        con.disconnect();
        parse(ja);
    } // response()

    private void print(final String s) {
        System.out.println(s);
    } // print(String)

    private void parse(final JsonArray ja) {
        print("Parsing JsonArray");
        int i = 0;
        for (final JsonElement je : ja) {
            if (++i % 500 == 0) {
                print(Integer.toString(i));
            } // if
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
    } // parse(JsonArray)

    private void connect(final JsonObject data) throws IOException, MalformedURLException {
        checkRes();
        final URL url = new URL(URL);
        print("Connecting to " + url.toString());
        con = (HttpURLConnection) url.openConnection();
        con.setDoInput(true);
        con.setDoOutput(true);
        con.setRequestMethod("POST");
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(con.getOutputStream());
            out.writeBytes(data.toString());
            print("Request sent to server");
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