package uk.ac.manchester.cs.patelt9.twitter.data;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.alchemyapi.api.AlchemyAPI;

public class SentimentAnalysis {
    private static final String DEFAULT_QUERY = "SELECT text, id FROM tweet WHERE sentiment IS NULL LIMIT 1000;";

    private final AlchemyAPI api;
    private ResultSet res;
    private volatile SqlConnector sql = null;
    private int count = 0;

    private static SentimentAnalysis sa = null;

    public static SentimentAnalysis getInstance() throws IOException, SQLException {
        if (sa == null) {
            sa = new SentimentAnalysis();
        } // if
        return sa;
    } // getInstance()

    private SentimentAnalysis() throws IOException, SQLException {
        api = AlchemyAPI.GetInstanceFromFile("alchemyapikey.txt");
        sql = SqlConnector.getInstance();
    } // SentimentAnalysis()

    private void setRes(final ResultSet res) {
        this.res = res;
    } // setRes(ResultSet)

    public void loadDataSet() {
        loadDataSet(DEFAULT_QUERY);
    } // loadDataSet()

    public void loadDataSet(final String sqlStatement) {
        try {
            setRes(sql.executeQuery(sqlStatement));
        } catch (final SQLException e) {
            e.printStackTrace();
        } // catch
    } // loadDataSet(String)

    public void analyseSentiment() {
        try {
            res.beforeFirst();
            while (res.next()) {
                final String tweet = res.getString(1);
                final Long id = res.getLong(2);
                // System.out.println(id + ": " + tweet);
                try {
                    final Document doc;
                    try {
                        doc = api.TextGetTextSentiment(tweet);
                    } catch (final IllegalArgumentException e) {
                        count += sql.executeUpdate("UPDATE tweet SET sentiment='none' WHERE id='"
                                + id + "';");
                        continue;
                    }
                    final Node sentimentNode = doc.getElementsByTagName("docSentiment").item(0);
                    if (sentimentNode != null && sentimentNode.getNodeType() == Node.ELEMENT_NODE) {
                        final Element sent = (Element) sentimentNode;
                        final String sentiment = sent.getElementsByTagName("type").item(0)
                                .getTextContent();
                        // System.out.println(sentiment);
                        if (!sentiment.equals("neutral")) {
                            final String sentimentScore = sent.getElementsByTagName("score")
                                    .item(0).getTextContent();
                            // System.out.println(sentimentScore);
                            count += sql.executeUpdate("UPDATE tweet SET sentiment='" + sentiment
                                    + "', sentiment_score='" + sentimentScore + "' WHERE id='" + id
                                    + "';");
                        } else {
                            count += sql.executeUpdate("UPDATE tweet SET sentiment='" + sentiment
                                    + "' WHERE id='" + id + "';");
                        } // else
                    } // if
                } catch (final XPathExpressionException e) {
                    e.printStackTrace();
                } catch (final IOException e) {
                    // e.printStackTrace();
                } catch (final SAXException e) {
                    e.printStackTrace();
                } catch (final ParserConfigurationException e) {
                    e.printStackTrace();
                } // catch
            } // while
        } catch (final DOMException e) {
            e.printStackTrace();
        } catch (final SQLException e) {
            e.printStackTrace();
        } // catch
    }// analyseSentiment()

    public void close() {
        System.out.println(Integer.toString(count));
        if (sql != null) {
            sql.close();
        } // if
        sa = null;
    } // close()
} // SentimentAnalysis
