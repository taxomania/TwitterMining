package uk.ac.manchester.cs.patelt9.twitter.thread;

import java.util.HashSet;
import java.util.Set;

import uk.ac.manchester.cs.patelt9.twitter.Tweet;
import uk.ac.manchester.cs.patelt9.twitter.listener.ParseListener;

import com.google.gson.JsonObject;

public abstract class ParseThread extends Thread {
    private final JsonObject jo;

    public ParseThread(final JsonObject jo) {
        this("Parse", jo);
    } // ParseThread()

    public ParseThread(final String s, final JsonObject jo) {
        super(s);
        this.jo = jo;
    } // ParseThread(String)

    @Override
    public final void run() {
        parse();
    } // run()

    public JsonObject getJo(){
        return jo;
    } // getJo()

    protected abstract void parse();

    private final Set<ParseListener> listeners = new HashSet<ParseListener>();

    protected final void notifyListeners(final Tweet t) {
        for (final ParseListener listener : listeners) {
            listener.onParseComplete(t);
        } // for
    } // notifyListeners(Tweet)

    protected final void notifyListeners(final long id) {
        for (final ParseListener listener : listeners) {
            listener.onParseComplete(id);
        } // for
    } // notifyListeners(long)

    public final void addListener(final ParseListener listener) {
        listeners.add(listener);
    } // addListener(ParseListener)

    public final void removeListener(final ParseListener listener) {
        listeners.remove(listener);
    } // removeListener(ParseListener)
} // ParseThread
