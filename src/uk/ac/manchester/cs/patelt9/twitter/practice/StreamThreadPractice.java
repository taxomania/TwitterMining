package uk.ac.manchester.cs.patelt9.twitter.practice;

import java.util.HashSet;
import java.util.Set;


import com.google.gson.JsonObject;

public abstract class StreamThreadPractice extends Thread {
    public StreamThreadPractice() {
        this("Stream");
    } // StreamThread()

    public StreamThreadPractice(final String s) {
        super(s);
    } // StreamThread(String)

    @Override
    public final void run() {
        parse();
    } // run()

    protected abstract void parse();

    private final Set<StreamListenerPractice> listeners = new HashSet<StreamListenerPractice>();

    protected final void notifyListeners(final JsonObject jo) {
        for (final StreamListenerPractice listener : listeners) {
            listener.onJsonReadComplete(jo);
        } // for
    } // notifyListeners(JsonObject)

    public final void addListener(final StreamListenerPractice listener) {
        listeners.add(listener);
    } // addListener(StreamListener)

    public final void removeListener(final StreamListenerPractice listener) {
        listeners.remove(listener);
    } // removeListener(StreamListener)
} // StreamThread

