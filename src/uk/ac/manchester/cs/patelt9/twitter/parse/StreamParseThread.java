package uk.ac.manchester.cs.patelt9.twitter.parse;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.util.PythonInterpreter;

import uk.ac.manchester.cs.patelt9.twitter.data.Tweet;
import uk.ac.manchester.cs.patelt9.twitter.data.User;

import com.google.gson.JsonObject;

public class StreamParseThread extends Thread {
    private final List<JsonObject> parseList = new ArrayList<JsonObject>();
    private static PyObject langDetector;
    private int objectsParsed;

    static {
        final PythonInterpreter py = new PythonInterpreter();
        String filepath = new File("pysrc/lang").getAbsolutePath();
        py.exec("import sys\n" + "sys.path.append('" + filepath + "')\n");
        filepath = new File("pysrc/guess_language").getAbsolutePath();
        py.exec("sys.path.append('" + filepath + "')\nfrom lang import getLanguage");
        langDetector = py.get("getLanguage");
    } // static

    public StreamParseThread() {
        this("ParseJson");
    } // StreamParseThread()

    public StreamParseThread(final String s) {
        super(s);
        objectsParsed = 0;
    } // StreamParseThread(String)

    @Override
    public final void run() {
        while (!isInterrupted()) {
            try {
                synchronized (parseList) {
                    performTask();
                } // synchronized
            } catch (final IndexOutOfBoundsException e) {
                continue;
            } // catch
        } // while
    } // run()

    @Override
    public void interrupt() {
        synchronized (parseList) {
            while (!parseList.isEmpty()) {
                performTask();
            } // while
        } // synchronized
        System.out.println(objectsParsed + " objects parsed");
        super.interrupt();
    } // interrupt()

    protected void performTask() {
        objectsParsed += parse(parseList.remove(0));
    } // performTask()

    public boolean addTask(final JsonObject object) {
        synchronized (parseList) {
            return parseList.add(object);
        } // synchronized
    } // addTask(JsonObject)

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

    private int parse(final JsonObject jo) {
        if (isTweetJsonObject(jo)) {
            final Tweet tweet = getTweet(jo);
            if (tweet != null) {
                notifyListeners(tweet);
            } // if
        } else {
            notifyListeners(getDeleteStatusId(jo));
        } // else
        return 1;
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
        if (!("en".equals(user.getAsJsonPrimitive("lang").getAsString()))) { return null; }
        final String tweet = jo.getAsJsonPrimitive("text").getAsString();
        final String language = langDetector.__call__(new PyString(tweet)).asString();
        if (!"English".equals(language)) {
            System.out.println(tweet);
            return null;
        } // SEEMS FAULTY
        final long userId = user.getAsJsonPrimitive("id_str").getAsLong();
        final String screenName = user.getAsJsonPrimitive("screen_name").getAsString();
        final long tweetId = jo.getAsJsonPrimitive("id_str").getAsLong();
        final String createdAt = parseCreatedAtForSql(jo.getAsJsonPrimitive("created_at")
                .getAsString());
        return new Tweet(tweetId, tweet, createdAt, new User(userId, screenName));
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
} // StreamParseThread