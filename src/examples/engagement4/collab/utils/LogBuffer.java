package engagement4.collab.utils;

import java.io.*;
import java.nio.*;
import java.util.logging.*;

public class LogBuffer extends Handler
{
    FileOutputStream fileOutputStream;
    PrintWriter printWriter;
    CharBuffer buf;
    String filename;
    String[] buffx;
    int buffxloc;
    
    public LogBuffer(final String fname) {
        this.buffx = new String[2];
        this.buffxloc = 0;
        this.filename = fname;
        if (this.filename == null || this.filename == "") {
            this.filename = "translogfile.txt";
        }
    }
    
    @Override
    public void publish(final LogRecord record) {
        if (!this.isLoggable(record)) {
            return;
        }
        this.buf = CharBuffer.allocate(2);
        try {
            this.fileOutputStream = new FileOutputStream(this.filename);
            this.printWriter = new PrintWriter(this.fileOutputStream);
            this.setFormatter(new XMLFormatter());
        }
        catch (Exception ex) {}
        try {
            this.buf.put(this.getFormatter().format(record));
        }
        catch (BufferOverflowException boe) {
            this.flush();
            this.printWriter.println(this.getFormatter().format(record));
        }
    }
    
    public void publish(final String m) {
        try {
            this.buffx[this.buffxloc] = m;
            ++this.buffxloc;
        }
        catch (ArrayIndexOutOfBoundsException e) {
            for (int i = 0; i < this.buffx.length; ++i) {
                final LogRecord lr = new LogRecord(Level.INFO, this.buffx[i]);
                this.publish(lr);
            }
            final LogRecord lr2 = new LogRecord(Level.INFO, m);
            this.publish(lr2);
            this.buffxloc = 0;
        }
    }
    
    @Override
    public void flush() {
        final char[] array = this.buf.array();
        this.printWriter.print(array);
        this.printWriter.flush();
        this.buf.clear();
    }
    
    @Override
    public void close() throws SecurityException {
        this.printWriter.close();
    }
}
