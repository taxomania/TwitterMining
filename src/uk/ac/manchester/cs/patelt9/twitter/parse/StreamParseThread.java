package uk.ac.manchester.cs.patelt9.twitter.parse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;

import uk.ac.manchester.cs.patelt9.twitter.data.Tweet;

import com.google.gson.JsonObject;

public class StreamParseThread extends Thread {
    private final JsonObject jo;

    public StreamParseThread(final JsonObject jo) {
        this("Parse", jo);
    } // ParseThread()

    public StreamParseThread(final String s, final JsonObject jo) {
        super(s);
        this.jo = jo;
    } // ParseThread(String)

    @Override
    public final void run() {
        parse();
    } // run()

    public interface ParseListener {
        void onParseComplete(Tweet t);
        void onParseComplete(long id);
    } // ParseListener

    private static final Set<ParseListener> listeners = new HashSet<ParseListener>();

    protected final void notifyListeners(final Tweet t) {
        synchronized (listeners) {
            for (final ParseListener listener : listeners) {
                listener.onParseComplete(t);
            } // for
        } // synchronized
    } // notifyListeners(Tweet)

    protected final void notifyListeners(final long id) {
        synchronized (listeners) {
            for (final ParseListener listener : listeners) {
                listener.onParseComplete(id);
            } // for
        } // synchronized
    } // notifyListeners(long)

    public final void addListener(final ParseListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        } // synchronized
    } // addListener(ParseListener)

    public final void removeListener(final ParseListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        } // synchronized
    } // removeListener(ParseListener)

    protected void parse() {
        if (isTweetJsonObject(jo)) {
            notifyListeners(getTweet(jo));
        } else {
            notifyListeners(getDeleteStatusId(jo));
        } // else
    } // parse()

    private boolean isTweetJsonObject(final JsonObject jo) {
        if (jo.toString().contains("{\"delete\":")) {
            return false;
        } else {
            return true;
        } // else
    } // isTweetJsonObject(JsonObject)

    private Tweet getTweet(final JsonObject jo) {
        final JsonObject user = jo.getAsJsonObject("user");
        final Long userId = user.getAsJsonPrimitive("id_str").getAsLong();
        final String screenName = user.getAsJsonPrimitive("screen_name").getAsString();
        final String tweet = jo.getAsJsonPrimitive("text").getAsString();
        final Long tweetId = jo.getAsJsonPrimitive("id_str").getAsLong();
        final String createdAt = parseCreatedAtForSql(jo.getAsJsonPrimitive("created_at")
                .getAsString());
        return new Tweet(tweetId, userId, screenName, tweet, createdAt);
    } // getTweet(JsonObject)

    private long getDeleteStatusId(final JsonObject jo) {
        return jo.getAsJsonObject("delete").getAsJsonObject("status").getAsJsonPrimitive("id_str")
                .getAsLong();
    } // getDeleteStatusId(JsonObject)

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
} // ParseThread
