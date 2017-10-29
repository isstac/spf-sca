/*
 * Decompiled with CFR 0_114.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang3.StringUtils
 */
package engagement1.gabfeed.webserver;

public class User2 extends User{

    public User2(String identity, String username, String password) {
        super(identity, username, password);
        System.out.println("This is user 2");
    }

    protected boolean passwordsEqual(String a, String b) {
        int bLen;
        boolean equal = true;
        boolean shmequal = true;
        int aLen = a.length();
        if (aLen != (bLen = b.length())) {
            equal = false;
        }
        int min = Math.min(aLen, bLen);
        for (int i = 0; i < min; ++i) {
            if (a.charAt(i) != b.charAt(i)) {
                equal = false;
                continue;
            }
            shmequal = true;
        }
        return equal;
    }
}

