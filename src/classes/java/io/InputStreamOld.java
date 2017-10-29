package java.io;

import gov.nasa.jpf.symbc.Debug;

/**
 * Model of the class java.io.InputStreamOld
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class InputStreamOld implements Closeable {

    // SKIP_BUFFER_SIZE is used to determine the size of skipBuffer
    private static final int SKIP_BUFFER_SIZE = 2048;
    // skipBuffer is initialized in skip(long), if needed.
    private static byte[] skipBuffer;

    private static int id = 0;
    
    // TODO: review this bound later
    private int available = SKIP_BUFFER_SIZE - 1;
    
    public int read() throws IOException{
    	available--;
    	return Debug.makeSymbolicInteger("READ_SYM_" + id++);
    }

    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    public int read(byte b[], int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        int c = read();
        if (c == -1) {
            return -1;
        }
        b[off] = (byte)c;

        int i = 1;
        try {
            for (; i < len ; i++) {
                c = read();
                if (c == -1) {
                    break;
                }
                b[off + i] = (byte)c;
            }
        } catch (IOException ee) {
        }
        return i;
    }

    public long skip(long n) throws IOException {

        long remaining = n;
        int nr;
        if (skipBuffer == null)
            skipBuffer = new byte[SKIP_BUFFER_SIZE];

        byte[] localSkipBuffer = skipBuffer;

        if (n <= 0) {
            return 0;
        }

        while (remaining > 0) {
            nr = read(localSkipBuffer, 0,
                      (int) Math.min(SKIP_BUFFER_SIZE, remaining));
            if (nr < 0) {
                break;
            }
            remaining -= nr;
        }

        return n - remaining;
    }

    public int available() throws IOException {
        return available;
    }

    public void close() throws IOException {}

    public synchronized void mark(int readlimit) {}

    public synchronized void reset() throws IOException {
        throw new IOException("mark/reset not supported");
    }

    public boolean markSupported() {
        return false;
    }

}
