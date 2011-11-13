package uk.ac.manchester.cs.patelt9.twitter.parse;

import org.w3c.dom.Document;

public class SentimentObject {
    private final Document doc;
    private final long id;

    public SentimentObject(final long id, final Document document) {
        doc = document;
        this.id = id;
    } // SentimentParseThread(long, Document)

    public Document getDoc() {
        return doc;
    } // getDoc()

    public long getId() {
        return id;
    } // getId()
} // SentimentObject
