package uk.ac.manchester.cs.patelt9.twitter.parse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class SentimentParseThread extends Thread {
    private final List<SentimentObject> parseList = new ArrayList<SentimentObject>();
    private int objectsParsed;

    public SentimentParseThread() {
        this("ParseSentiment");
    } // StreamParseThread()

    public SentimentParseThread(final String s) {
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

    public boolean addTask(final SentimentObject object) {
        synchronized (parseList) {
            return parseList.add(object);
        } // synchronized
    } // addTask(JsonObject)

    //@formatter:off
    public interface ParseListener {
        void onParseComplete(long id, String sentiment);
        void onParseComplete(long id, String sentiment, String score);
    } // ParseListener
    //@formatter:on

    private static final Set<ParseListener> listeners = new HashSet<ParseListener>();

    protected final void notifyListeners(final long id, final String sentiment) {
        synchronized (listeners) {
            for (final ParseListener listener : listeners) {
                listener.onParseComplete(id, sentiment);
            } // for
        } // synchronized
    } // notifyListeners(Tweet)

    protected final void notifyListeners(final long id, final String sentiment,
            final String sentimentScore) {
        synchronized (listeners) {
            for (final ParseListener listener : listeners) {
                listener.onParseComplete(id, sentiment, sentimentScore);
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

    protected int parse(final SentimentObject sen) {
        final Node sentimentNode = sen.getDoc().getElementsByTagName("docSentiment").item(0);
        if (sentimentNode != null && sentimentNode.getNodeType() == Node.ELEMENT_NODE) {
            final Element sent = (Element) sentimentNode;
            final String sentiment = sent.getElementsByTagName("type").item(0).getTextContent();
            if (!sentiment.equals("neutral")) {
                final String sentimentScore = sent.getElementsByTagName("score").item(0)
                        .getTextContent();
                notifyListeners(sen.getId(), sentiment, sentimentScore);
            } else {
                notifyListeners(sen.getId(), sentiment);
            } // else
        } // if
        return 1;
    } // parse()
} // SentimentParseThread
