package uk.ac.manchester.cs.patelt9.twitter;

import java.util.HashSet;
import java.util.Set;

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
} // StreamThread

