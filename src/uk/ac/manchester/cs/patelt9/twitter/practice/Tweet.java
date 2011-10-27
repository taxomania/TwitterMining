package uk.ac.manchester.cs.patelt9.twitter.practice;

public class Tweet {
    private final String text;
    private final String createdAt;

    public Tweet(final String content, final String created) {
        text = content;
        createdAt = created;
    } // Tweet(final String, final String)

    public String getText(){
        return text;
    } // getText()

    public String getCreatedAt(){
        return createdAt;
    } // getCreatedAt

    @Override
    public String toString() {
        return getText();
    } // toString

} // class Tweet
