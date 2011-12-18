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
import org.xeustechnologies.googleapi.spelling.Language;
import org.xeustechnologies.googleapi.spelling.SpellChecker;
import org.xeustechnologies.googleapi.spelling.SpellCorrection;
import org.xeustechnologies.googleapi.spelling.SpellRequest;
import org.xeustechnologies.googleapi.spelling.SpellResponse;

import uk.ac.manchester.cs.patelt9.twitter.data.Tweet;
import uk.ac.manchester.cs.patelt9.twitter.data.User;

import com.google.gson.JsonObject;

public class StreamParseThread extends Thread {
    private static PyObject langDetector;

    private final List<JsonObject> parseList = new ArrayList<JsonObject>();
    private final SpellChecker spellChecker;

    private int objectsParsed;

    static {
        final PythonInterpreter py = new PythonInterpreter();
        final String path = "/opt/local/Library/Frameworks/Python.framework/Versions/2.6/lib/python2.6/site-packages";
        final String filepath = new File("pysrc/lang").getAbsolutePath();
        py.exec("import sys\n");
        py.exec("sys.path.append('" + path + "')\n");
        py.exec("sys.path.append('" + filepath + "')\n");
        py.exec("from lang import getLanguage");
        langDetector = py.get("getLanguage");
    } // static

    public StreamParseThread() {
        this("ParseStream");
    } // StreamParseThread()

    public StreamParseThread(final String s) {
        super(s);
        objectsParsed = 0;
        spellChecker = new SpellChecker(Language.ENGLISH);
        spellChecker.setOverHttps(true);
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

    //@formatter:off
    public interface ParseListener {
        void onParseComplete(Tweet t);
        void onParseComplete(long id);
    } // ParseListener
    //@formatter:on

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

    // TESTING THIS
    @SuppressWarnings("unused")
    private void spellCheck(final String s) {
        final SpellRequest req = new SpellRequest(s);
        req.setIgnoreWordsWithDigits(true);
        final SpellResponse resp = spellChecker.check(req);
        try {
            for (final SpellCorrection corr : resp.getCorrections()) {
                System.out.println(corr.getValue());
            } // for
        } catch (final NullPointerException e) {
            // No spelling errors
        } // catch
    } // spellCheck(String)

    private Tweet getTweet(final JsonObject jo) {
        final JsonObject user = jo.getAsJsonObject("user");
        if (!("en".equals(user.getAsJsonPrimitive("lang").getAsString()))) { return null; }
        final JsonObject retweetedStatus = jo.getAsJsonObject("retweeted_status");
        if (retweetedStatus != null) { return getTweet(retweetedStatus); }
        final String tweet = jo.getAsJsonPrimitive("text").getAsString();
        final String language = langDetector.__call__(new PyString(tweet)).asString();
        if (!"English".equals(language)) {
            // System.out.println(tweet);
            return null;
        } // slightly inaccurate

        // spellCheck(tweet); // ONLY TESTING AT THIS STAGE

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
