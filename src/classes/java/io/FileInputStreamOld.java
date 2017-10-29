package java.io;

import java.nio.channels.FileChannel;

/**
 * Model of the class java.io.FileInputStreamOld
 *
 * @author Quoc-Sang Phan <sang.phan@sv.cmu.edu>
 *
 */
public
class FileInputStreamOld extends InputStreamOld
{
    /* File Descriptor - handle to the open file */
    private FileDescriptor fd;

    private FileChannel channel = null;

    public FileInputStreamOld(String name) throws FileNotFoundException {
        this(name != null ? new File(name) : null);
    }

    public FileInputStreamOld(File file) throws FileNotFoundException {
        // Assume that we can always open a symbolic file?
    }

    public FileInputStreamOld(FileDescriptor fdObj) {
    	// Assume that we can always open a symbolic file?
    }

    public void close() throws IOException {
    	// Do not need to close symbolic stream
    }

    public final FileDescriptor getFD() throws IOException {
        if (fd != null) return fd;
        throw new IOException();
    }

    public FileChannel getChannel() {
        synchronized (this) {
            if (channel == null) {
                //TODO: channel = FileChannelImpl.open(fd, true, false, this);

                /*
                 * Increment fd's use count. Invoking the channel's close()
                 * method will result in decrementing the use count set for
                 * the channel.
                 */
                // TODO: fd.incrementAndGetUseCount();
            }
            return channel;
        }
    }

}
