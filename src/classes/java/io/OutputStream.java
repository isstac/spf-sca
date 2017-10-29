package java.io;

/**
 * Model of the class java.io.OutputStreamOld
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class OutputStream extends Object implements Closeable,
		Flushable {

	public static final int SOCKET = 100;
	public static final int FILE = 101;

	protected int source; // indicate the source of the stream

	public OutputStream() {
	}

	public OutputStream(int source) {
		this.source = source;
	}

	public int getSource() {
		return source;
	}

	public void write(int b) throws IOException{
	}

    public void write(byte b[]) throws IOException {
        write(b, 0, b.length);
    }

    public void write(byte b[], int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if ((off < 0) || (off > b.length) || (len < 0) ||
                   ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        for (int i = 0 ; i < len ; i++) {
            write(b[off + i]);
        }
    }

	public void flush() throws IOException {
	}

    public void close() throws IOException {
    }
}
