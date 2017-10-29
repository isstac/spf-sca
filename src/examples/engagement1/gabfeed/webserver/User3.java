/*
 * Decompiled with CFR 0_114.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang3.StringUtils
 */
package engagement1.gabfeed.webserver;

import java.util.Random;

public class User3 extends User {

    public User3(String identity, String username, String password) {
    	super(identity, username, password);
    	System.out.println("This is user 3");
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
        int i = 0;
        while (i < min) {
            Random randomNumberGeneratorInstance = new Random();
            while (i < min && randomNumberGeneratorInstance.nextDouble() < 0.5) {
                if (a.charAt(i) != b.charAt(i)) {
                    equal = false;
                } else {
                    shmequal = true;
                }
                ++i;
            }
        }
        return equal;
    }
}

