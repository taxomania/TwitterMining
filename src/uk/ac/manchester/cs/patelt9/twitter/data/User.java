package uk.ac.manchester.cs.patelt9.twitter.data;

/**
 *
 * Represent Twitter users
 *
 * @author Tariq Patel
 *
 */
public final class User {
    private final long id;
    private final String username;

    /**
     * Class constructor
     *
     * @param id
     *            The user's id on Twitter
     *
     * @param name
     *            The user's Twitter screen name
     */
    public User(final long id, final String name) {
        this.id = id;
        username = name;
    } // User(long, String)

    /**
     * @return Twitter user id
     */
    public long getId() {
        return id;
    } // getId()

    /**
     * @return Twitter user screen name
     */
    public String getUsername() {
        return username;
    } // getUsername()
} // User
