package uk.ac.manchester.cs.patelt9.twitter.practice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

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

import uk.ac.manchester.cs.patelt9.twitter.TwitterUser;

public class APITest {
    private static final String GET_PUBLIC_TIMELINE_URL =
            "http://api.twitter.com/1/statuses/public_timeline.xml?trim_user=true";

    private static final Map<Long, TwitterUser> tweeters = new HashMap<Long, TwitterUser>();

    private static HttpResponse getTwitterPosts() {
        final HttpClient client = new DefaultHttpClient();
        try {
            final HttpGet request = new HttpGet(GET_PUBLIC_TIMELINE_URL);
            try {
                return client.execute(request);
            } catch (final ClientProtocolException e) {
                e.printStackTrace();
            } catch (final IOException e) {
                e.printStackTrace();
            } // catch
        } catch (final IllegalArgumentException e) {
            e.printStackTrace();
        } // catch
        return null;
    } // getTwitterPosts()

    private static void printTweets(final HttpResponse response) {
        // Error checking
        if (response == null) {
            System.err.println("There was a problem making the HTTP request");
            return;
        } // if
        if (response.getStatusLine().getStatusCode() != 200) {
            System.err.println("There was a problem connecting to the server");
            return;
        } // if

        final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        NodeList userIds = null, tweets = null;
        InputStream in = null;
        try {
            in = response.getEntity().getContent();

            // printXmlResponse(in); // Used for checking format of response
            try {
                final DocumentBuilder docBuillder = docBuilderFactory.newDocumentBuilder();
                final Document doc = docBuillder.parse(in);
                userIds = doc.getElementsByTagName("user");
                tweets = doc.getElementsByTagName("text");
            } catch (final ParserConfigurationException e) {
                e.printStackTrace();
            } catch (final SAXException e) {
                e.printStackTrace();
            } // catch
        } catch (final IOException e) {
            e.printStackTrace();
        } // catch
        finally {
            try {
                if (in != null) {
                    in.close();
                } // if
            } catch (final IOException e) {
                e.printStackTrace();
            } // catch
        } // finally

        if (userIds == null || tweets == null) return;

        // Links tweets to user through the map
        for (int i = 0; i < userIds.getLength(); i++) {
            final long id = parseUserId(userIds.item(i));
            final TwitterUser tweeter;
            if (tweeters.containsKey(id)) {
                tweeter = tweeters.get(id);
            } else {
                tweeter = new TwitterUser(id);
                tweeters.put(id, tweeter);
            } // else
            tweeter.addTweet(tweets.item(i).getTextContent());

            // For visualisation only
            System.out.println(Long.toString(id) + ": " + tweets.item(i).getTextContent());
        } // for
    } // printTweets(HttpResponse)

    public static void main(final String[] args) {
        printTweets(getTwitterPosts());

        // Test feature
        for (TwitterUser t : tweeters.values()){
            System.out.println(t);
        }
    } // main(String[])

    private static final long parseUserId(final Node user) {
        return Long.parseLong(user.getTextContent().replaceAll(" ", "").replaceAll("\n", ""));
    } // parseUserId(Node)

    // This is used to check the xml response from Twitter
    @SuppressWarnings("unused")
    private static void printXmlResponse(final InputStream in) throws IOException {
        final BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String s;
        while ((s = br.readLine()) != null) {
            System.out.println(s);
        } // while
    } // printResponse(InputStream)
} // APITest
