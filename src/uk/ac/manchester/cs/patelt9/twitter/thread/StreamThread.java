package uk.ac.manchester.cs.patelt9.twitter.thread;

import java.util.HashSet;
import java.util.Set;

import uk.ac.manchester.cs.patelt9.twitter.listener.StreamListener;

import com.google.gson.JsonObject;

public abstract class StreamThread extends Thread {
    public StreamThread() {
        this("Stream");
    } // StreamThread()

    public StreamThread(final String s) {
        super(s);
    } // StreamThread(String)

    @Override
    public final void run() {
        parse();
    } // run()

    protected abstract void parse();

    private final Set<StreamListener> listeners = new HashSet<StreamListener>();

    protected final void notifyListeners(final JsonObject jo) {
        for (final StreamListener listener : listeners) {
            listener.onJsonReadComplete(jo);
        } // for
    } // notifyListeners(JsonObject)

    public final void addListener(final StreamListener listener) {
        listeners.add(listener);
    } // addListener(StreamListener)

    public final void removeListener(final StreamListener listener) {
        listeners.remove(listener);
    } // removeListener(StreamListener)
} // StreamThread

