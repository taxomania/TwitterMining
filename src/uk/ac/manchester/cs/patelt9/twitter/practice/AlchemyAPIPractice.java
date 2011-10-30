package uk.ac.manchester.cs.patelt9.twitter.practice;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import uk.ac.manchester.cs.patelt9.twitter.data.SqlConnector;

import com.alchemyapi.api.AlchemyAPI;

public class AlchemyAPIPractice {

    public static void main(final String[] args) {
        final AlchemyAPI api;
        try {
            api = AlchemyAPI.GetInstanceFromFile("alchemyapikey.txt");
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
            return;
        } catch (final IOException e) {
            e.printStackTrace();
            return;
        } // catch
        final SqlConnector sql = SqlConnector.getInstance();
        try {
            final ResultSet res = sql
                    .executeQuery("SELECT t.text, t.id FROM user u, tweet t WHERE u.id = t.user_id AND u.username='taxomania';");
            res.beforeFirst();
            while (res.next()) {
                final String tweet = res.getString(1);
                final Long id = res.getLong(2);
                try {
                    final Document doc = api.TextGetTextSentiment(tweet);
                    final Node sentimentNode = doc.getElementsByTagName("docSentiment").item(0);
                    if (sentimentNode != null && sentimentNode.getNodeType() == Node.ELEMENT_NODE) {
                        final Element sent = (Element) sentimentNode;
                        final String sentiment = sent.getElementsByTagName("type").item(0)
                                .getTextContent();
                        System.out.println(sentiment);
                        final String sentimentScore = sent.getElementsByTagName("score").item(0)
                                .getTextContent();
                        System.out.println(sentimentScore);
                        System.out.println(sql.executeUpdate("UPDATE tweet SET sentiment='"
                                + sentiment + "', sentiment_score='" + sentimentScore
                                + "' WHERE id='" + id + "';")
                                + " rows updated");
                    } // if
                } catch (final XPathExpressionException e) {
                    e.printStackTrace();
                } catch (final IOException e) {
                    e.printStackTrace();
                } catch (final SAXException e) {
                    e.printStackTrace();
                } catch (final ParserConfigurationException e) {
                    e.printStackTrace();
                } // catch
            } // while
        } catch (final SQLException e) {
            e.printStackTrace();
        } // catch
        sql.close();
    } // main(String)
} // AlchemyAPIPractice
