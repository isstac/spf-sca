package engagement1.gabfeed.webserver;

public abstract class User {
	
	public static final int MIN_PASSWORD_LENGTH = 7;
    public static final int MAX_PASSWORD_LENGTH = 64;
    private final String identity;
    private final String username;
    private final String password;
    
    public User(final String identity, final String username, final String password) {
        this.identity = identity;
        this.username = username;
        this.password = password;
    }
    
    public String getIdentity() {
        return this.identity;
    }
    
    public String getUsername() {
        return this.username;
    }
    
    public String getPassword() {
        return this.password;
    }
    
    public boolean matches(final String username, final String password) {
        return this.username.equals((Object)username) & this.passwordsEqual(this.password, password);
    }
    
    protected abstract boolean passwordsEqual(String a, String b) ;
}
