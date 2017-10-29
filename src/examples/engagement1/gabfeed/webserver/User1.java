package engagement1.gabfeed.webserver;

public class User1 extends User
{
    
    public User1(final String identity, final String username, final String password) {
        super(identity, username, password);
        System.out.println("This is user 1");
    }
    
    protected boolean passwordsEqual(final String a, final String b) {
        boolean equal = true;
        boolean shmequal = true;
        final int aLen = a.length();
        final int bLen = b.length();
        if (aLen != bLen) {
            equal = false;
        }
        for (int min = Math.min(aLen, bLen), i = 0; i < min; ++i) {
            if (a.charAt(i) != b.charAt(i)) {
                equal = false;
            }
            else {
                shmequal = true;
            }
        }
        return equal;
    }
}
