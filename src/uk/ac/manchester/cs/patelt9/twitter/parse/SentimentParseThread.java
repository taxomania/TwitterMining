package uk.ac.manchester.cs.patelt9.twitter.parse;

import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class SentimentParseThread extends Thread {
    private final Document doc;
    private final long id;

    public SentimentParseThread(final long id, final Document document) {
        this("ParseSentiment", id, document);
    } // SentimentParseThread()

    public SentimentParseThread(final String s, final long id, final Document document) {
        super(s);
        doc = document;
        this.id = id;
    } // SentimentParseThread(String, Document)

    @Override
    public final void run() {
        parse();
    } // run()

    public interface ParseListener {
        void onParseComplete(long id, String sentiment);

        void onParseComplete(long id, String sentiment, String score);
    } // ParseListener

    private static final Set<ParseListener> listeners = new HashSet<ParseListener>();

    protected final void notifyListeners(final String sentiment) {
        synchronized (listeners) {
            for (final ParseListener listener : listeners) {
                listener.onParseComplete(id, sentiment);
            } // for
        } // synchronized
    } // notifyListeners(Tweet)

    protected final void notifyListeners(final String sentiment, final String sentimentScore) {
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

    protected void parse() {
        final Node sentimentNode = doc.getElementsByTagName("docSentiment").item(0);
        System.out.println(sentimentNode.getTextContent());
        if (sentimentNode != null && sentimentNode.getNodeType() == Node.ELEMENT_NODE) {
            final Element sent = (Element) sentimentNode;
            final String sentiment = sent.getElementsByTagName("type").item(0).getTextContent();
            System.out.println(sentiment);
            if (!sentiment.equals("neutral")) {
                final String sentimentScore = sent.getElementsByTagName("score").item(0)
                        .getTextContent();
                System.out.println(sentimentScore);
                notifyListeners(sentiment, sentimentScore);
            } else {
                notifyListeners(sentiment);
            } // else
        } // if
    } // parse()
} // SentimentParseThread
