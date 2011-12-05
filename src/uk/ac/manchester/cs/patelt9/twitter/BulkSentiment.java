package uk.ac.manchester.cs.patelt9.twitter;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;

public class BulkSentiment {
    private static final String URL = "http://partners-v1.twittersentiment.appspot.com/api/bulkClassifyJson";
    private final JsonObject postJson = new JsonObject();
    private final JsonArray ja = new JsonArray();

    private HttpURLConnection con = null;

    public static void main(final String[] args) throws IOException {
        new BulkSentiment();
    }

    private BulkSentiment() throws IOException {
        JsonObject jo = new JsonObject();
        jo.add("text", new JsonPrimitive("abc"));
        jo.add("id", new JsonPrimitive(123));
        ja.add(jo);
        jo = new JsonObject();
        jo.add("text", new JsonPrimitive("abcaf"));
        jo.add("id", new JsonPrimitive(122));
        ja.add(jo);
        postJson.add("data", ja);
        connect(new URL(URL));
        response();
    }

    private JsonReader j;

    private void response() throws IOException {
        j = new JsonReader(new BufferedReader(new InputStreamReader(con.getInputStream())));
        final JsonArray ja = new JsonParser().parse(j).getAsJsonObject().getAsJsonArray("data");
        j.close();
        con.disconnect();
        System.out.println(ja.toString());

    }

    private void connect(final URL url) throws IOException {
        System.out.println("Connecting to " + url.toString());
        con = (HttpURLConnection) url.openConnection();
        con.setDoInput(true);
        con.setDoOutput(true);
        con.setRequestMethod("POST");
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(con.getOutputStream());
            out.writeBytes(postJson.toString());
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

}