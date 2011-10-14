package uk.ac.manchester.cs.patelt9.twitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class APITest {
    private static final String GET_PUBLIC_TIMELINE_URL =
            "http://api.twitter.com/1/statuses/public_timeline.xml?trim_user=true";

    public static void main(String[] args) {
        final HttpClient client = new DefaultHttpClient();
        HttpResponse response = null;
        try {
            final HttpGet request = new HttpGet(GET_PUBLIC_TIMELINE_URL);
            try {
                response = client.execute(request);
            } catch (final ClientProtocolException e) {
                e.printStackTrace();
            } catch (final IOException e) {
                e.printStackTrace();
            } // catch
        } catch (final IllegalArgumentException e) {
            e.printStackTrace();
        } // catch

        if (response == null) return;

        final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        NodeList userIds = null, statusTexts = null;
        InputStream in = null;
        try {
            in = response.getEntity().getContent();

            //printResponse(in);
            try {
                final DocumentBuilder docBuillder = docBuilderFactory.newDocumentBuilder();
                final Document doc = docBuillder.parse(in);
                userIds = doc.getElementsByTagName("user");
                statusTexts = doc.getElementsByTagName("text");
            } catch (final ParserConfigurationException e) {
                e.printStackTrace();
            } catch (final SAXException e) {
                e.printStackTrace();
            } // catch
        } catch (IOException e) {
            e.printStackTrace();
        } // catch
        finally {
            try {
                if (in != null) {
                    in.close();
                } // if
            } catch (IOException e) {
                e.printStackTrace();
            } // catch
        } // finally

        for (int i = 0; i < userIds.getLength(); i++) {
            System.out.println(parseUserId(userIds.item(i)) + ": "
                    + statusTexts.item(i).getTextContent());
        } // for
    } // main(String[])

    private static String parseUserId(final Node user) {
        String id = user.getTextContent();
        id = id.replaceAll(" ", "");
        return id.replaceAll("\n", "");
    } // parseUserId(Node)

    @SuppressWarnings("unused")
    private static void printResponse(final InputStream in) throws IOException {
        final BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String s;
        while ((s = br.readLine()) != null) {
            System.out.println(s);
        } // while
    } // printResponse(InputStream)
} // APITest
