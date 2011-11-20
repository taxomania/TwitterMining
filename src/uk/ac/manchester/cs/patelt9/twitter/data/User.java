package uk.ac.manchester.cs.patelt9.twitter.data;

public final class User {
    private final Long id;
    private final String username;

    public User(final long id, final String name) {
        this.id = new Long(id);
        username = name;
    } // User(long, String)

    public Long getId() {
        return id;
    } // getId()

    public String getUsername() {
        return username;
    } // getUsername()
} // User
