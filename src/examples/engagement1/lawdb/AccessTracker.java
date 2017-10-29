package engagement1.lawdb;

import java.net.*;
import java.util.logging.*;
import java.io.*;

public class AccessTracker
{
    public int loc;
    public int[] ids;
    
    AccessTracker() {
        this.loc = 0;
        this.ids = new int[10];
    }
    
    void add(final String lastaccessinfolog, final String toString, final int id) {
        try {
            this.ids[this.loc] = id;
            ++this.loc;
        }
        catch (ArrayIndexOutOfBoundsException ae) {
            this.loc = 0;
        }
    }
    
    void clean() {
        try {
            final DSystemHandle sys = new DSystemHandle("127.0.0.1", 6669);
            // final DFileHandle fhlog = new DFileHandle("lastaccess.log", sys);
            // fhlog.setContents(this.ids.toString());
            // fhlog.storefast(null, null);
        }
        // catch (IOException ex) {
        catch (Exception ex) {
            Logger.getLogger(AccessTracker.class.getName()).log(Level.SEVERE, (String)null, (Throwable)ex);
        }
    }
}
