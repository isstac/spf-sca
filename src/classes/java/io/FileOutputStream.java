package java.io;

import java.nio.channels.FileChannel;

/**
 * Model of the class java.io.FileOutputStreamOld
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public class FileOutputStream extends OutputStream {

	private FileDescriptor fd;

	private FileChannel channel = null;

	public FileOutputStream(String name) throws FileNotFoundException {
		this(name != null ? new File(name) : null, false);
	}

	public FileOutputStream(String name, boolean append)
			throws FileNotFoundException {
		this(name != null ? new File(name) : null, append);
	}

	public FileOutputStream(File file) throws FileNotFoundException {
		this(file, false);
	}

	public FileOutputStream(File file, boolean append)
			throws FileNotFoundException {
		source = OutputStream.FILE;
	}

	public FileOutputStream(FileDescriptor fdObj) {
		source = OutputStream.FILE;
	}

	public void close() throws IOException {
	}

	public final FileDescriptor getFD() throws IOException {
		if (fd != null)
			return fd;
		throw new IOException();
	}

	public FileChannel getChannel() {
		synchronized (this) {
			if (channel == null) {
				// TODO channel = FileChannelImpl.open(fd, false, true, this,
				// append);

				/*
				 * Increment fd's use count. Invoking the channel's close()
				 * method will result in decrementing the use count set for the
				 * channel.
				 */
				// TODO fd.incrementAndGetUseCount();
			}
			return channel;
		}
	}

	protected void finalize() throws IOException {
	}

}
