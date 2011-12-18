package uk.ac.manchester.cs.patelt9.twitter.stream;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.net.ssl.HttpsURLConnection;

import uk.ac.manchester.cs.patelt9.twitter.data.Tweet;
import uk.ac.manchester.cs.patelt9.twitter.data.db.DictionarySQLConnector;
import uk.ac.manchester.cs.patelt9.twitter.data.db.task.InsertKeywordTask;
import uk.ac.manchester.cs.patelt9.twitter.data.db.task.InsertTask;

import com.mongodb.MongoException;

public class StreamingApiFilter extends StreamingApi {
    // URL for Twitter Streaming API filter; filter by software here
    private static final String TWITTER_STREAM_API = "https://stream.twitter.com/1/statuses/filter.json";
    private static final int COUNTER_INTERVAL = 200;
    private static final String QUERY_PREFIX = "track=";

    private static StreamingApiFilter stream = null;

    protected String keyword = "";

    public static StreamingApiFilter getInstance() throws MongoException, UnknownHostException,
            SQLException {
        if (stream == null) {
            stream = new StreamingApiFilter();
        } // if
        return stream;
    } // getInstance(String[])

    private StreamingApiFilter() throws UnknownHostException, MongoException, SQLException {
        super(TWITTER_STREAM_API, COUNTER_INTERVAL);
        setKeyword();
    } // StreamingApiFilterPost(String[])

    private void setKeyword() throws SQLException {
        final ResultSet filters = DictionarySQLConnector.getInstance().selectAll();
        filters.beforeFirst();
        while (filters.next()) {
            keyword += filters.getString(1) + ",";
        } // while
        keyword = keyword.substring(0, keyword.length() - 1);
    } // setKeyword()

    @Override
    protected void connect(final HttpsURLConnection con) throws IOException {
        con.setDoInput(true);
        con.setDoOutput(true);
        con.setRequestMethod("POST");
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(con.getOutputStream());
            out.writeBytes(QUERY_PREFIX + keyword);
            // System.out.println("Filtering by " + keyword.replaceAll(",", ", "));
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

    @Override
    protected InsertTask createInsertTask(final Tweet t) {
        return new InsertKeywordTask(t, "latest"); // No longer feasible to store keyword
                                                   // Store 'latest' to differentiate from old
                                                   // tweets
    } // createInsertTask(Tweet)
} // StreamingApiFilter
