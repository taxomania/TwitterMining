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

    Set<StreamListener> listeners = new HashSet<StreamListener>();

    public void notifyListeners(final JsonObject jo) {
        for (final StreamListener listener : listeners) {
            listener.onJsonReadComplete(jo);
        } // for
    } // notifyListeners(JsonObject)

    public void addListener(final StreamListener listener) {
        listeners.add(listener);
    } // addListener(StreamListener)
} // StreamThread

