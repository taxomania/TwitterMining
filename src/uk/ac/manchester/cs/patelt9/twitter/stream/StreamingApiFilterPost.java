package uk.ac.manchester.cs.patelt9.twitter.stream;

import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.SQLException;

import javax.net.ssl.HttpsURLConnection;

public class StreamingApiFilterPost extends StreamingApiFilter {
    private static final int COUNTER_INTERVAL = 200;
    private static final String QUERY_PREFIX = "track=";

    private static StreamingApiFilterPost stream = null;

    public static StreamingApiFilterPost getInstance(final String[] keywords) throws SQLException {
        if (stream == null) {
            stream = new StreamingApiFilterPost(keywords);
        } // if
        return stream;
    } // getInstance(String[])

    public static StreamingApiFilterPost getInstance() throws SQLException {
        return getInstance(new String[] { "software", "app" });
    } // getInstance()

    private StreamingApiFilterPost(final String[] keywords) throws SQLException {
        super(COUNTER_INTERVAL);
        final int queryArgs = keywords.length;
        for (int i = 0; i < queryArgs - 1; i++) {
            keyword = keyword.concat(keywords[i] + ",");
        } // for
        keyword = keyword.concat(keywords[queryArgs - 1]);
    } // StreamingApiFilterPost(String[])

    @Override
    protected void connect(final HttpsURLConnection con) throws IOException {
        DataOutputStream out = null;
        con.setDoInput(true);
        con.setDoOutput(true);
        try {
            con.setRequestMethod("POST");
            out = new DataOutputStream(con.getOutputStream());
            out.writeBytes(QUERY_PREFIX + keyword);
            System.out.println("Filtering by " + keyword.replaceAll(",", ", "));
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
} // StreamingApiFilterPost
