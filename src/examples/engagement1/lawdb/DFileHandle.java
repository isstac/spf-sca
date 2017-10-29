package engagement1.lawdb;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class DFileHandle
{
    String name;
    DSystemHandle syshandle;
    private String contents;
    
    public DFileHandle(final String name, final DSystemHandle syshandle) {
        this.name = name;
        this.syshandle = syshandle;
    }
    
    public void setContents(final String contents) {
        this.contents = contents;
    }
    
    public static void main(final String[] args) throws IOException {
        DSystemHandle sys = sys = new DSystemHandle("127.0.0.1", 6666);
        final String[] f = { "a", "b", "c" };
        getContents(f, sys);
    }
    
    public static String getContents(final String[] names, final DSystemHandle syshandle) throws IOException {
        // final FileListDTO dto = new FileListDTO(names);
        final Message msg = new Message();
        msg.setType(7);
       // msg.setData(dto);
        final Socket socket = new Socket();
        socket.setReuseAddress(true);
        socket.bind((SocketAddress)null);
        // socket.connect((SocketAddress)new InetSocketAddress(DSystem.ADDRESS, DSystem.PORT));
        final ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        out.writeObject((Object)msg);
        out.flush();
        final BufferedReader datafromserver = new BufferedReader((Reader)new InputStreamReader(socket.getInputStream()));
        String data = "";
        String fromServer;
        while ((fromServer = datafromserver.readLine()) != null && !fromServer.equals("done.")) {
            data += fromServer;
        }
        datafromserver.close();
        socket.close();
        return fromServer;
    }
    
    public void store(Socket socket, ObjectOutputStream out) throws IOException {
        // final DistributedFile file = new DistributedFile(this.name);
        // final FileStreamDTO dto = new FileStreamDTO(file, 1);
        final Message msg = new Message();
        msg.setType(5);
        // msg.setData(dto);
        socket = null;
        if (socket == null) {
            socket = new Socket();
            socket.setReuseAddress(true);
            socket.bind((SocketAddress)null);
            // socket.connect((SocketAddress)new InetSocketAddress(DSystem.ADDRESS, DSystem.PORT));
        }
        out = null;
        if (out == null) {
            out = new ObjectOutputStream(socket.getOutputStream());
        }
        out.writeObject((Object)msg);
        final byte[] contentInBytes = this.contents.getBytes();
        final ByteArrayInputStream in = new ByteArrayInputStream(contentInBytes);
        final byte[] bytes = new byte[10240];
        for (int tam = in.read(bytes); tam != -1; tam = in.read(bytes)) {
            out.write(bytes, 0, tam);
        }
        out.flush();
        in.close();
        out.close();
        socket.close();
    }
    
    public void storefast(Socket socket, ObjectOutputStream out) throws IOException {
        // final DistributedFile file = new DistributedFile(this.name);
        System.out.println("sending:" + this.name);
        // final FileStreamDTO dto = new FileStreamDTO(file, 1);
        final Message msg = new Message();
        msg.setType(5);
        // msg.setData(dto);
        socket = null;
        if (socket == null) {
            socket = new Socket();
            socket.setReuseAddress(true);
            socket.bind((SocketAddress)null);
            // socket.connect((SocketAddress)new InetSocketAddress(DSystem.ADDRESS, DSystem.PORT));
            try {
                // final FileOutputStream fout = new FileOutputStream(file.getName());
                // final StringReader fin = new StringReader(file.getContents());
                final byte[] bytes = this.contents.getBytes();
               // fout.write(bytes, 0, bytes.length);
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        out = null;
        if (out == null) {
            out = new ObjectOutputStream(socket.getOutputStream());
        }
        out.writeObject((Object)msg);
        final byte[] contentInBytes = this.contents.getBytes();
        final ByteArrayInputStream in = new ByteArrayInputStream(contentInBytes);
        final byte[] bytes = new byte[10240];
        for (int tam = in.read(bytes); tam != -1; tam = in.read(bytes)) {
            out.write(bytes, 0, tam);
        }
        out.flush();
        in.close();
        out.close();
        socket.close();
        try {
            //final File l = new File(file.getName());
            // l.delete();
        }
        catch (Exception ex) {}
    }
    
    public String retrieve() throws IOException {
        // final DistributedFile file = new DistributedFile(this.name);
        /// final FileStreamDTO dto = new FileStreamDTO(file, 1);
        final Message msg = new Message();
        msg.setType(6);
        // msg.setData(dto);
        // final Socket socket = new Socket(DSystem.ADDRESS, DSystem.PORT);
        // final ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        // out.writeObject((Object)msg);
        // out.flush();
        // final BufferedReader datafromserver = new BufferedReader((Reader)new InputStreamReader(socket.getInputStream()));
        String data = "";
        String fromServer;
        // while ((fromServer = datafromserver.readLine()) != null && !fromServer.equals("done.")) {
        //     data += fromServer;
        // }
        // socket.close();
        return data;
    }
    
    public static boolean exists() throws IOException {
        final Message msg = new Message();
        msg.setType(5);
        // final Socket socket = new Socket(DSystem.ADDRESS, DSystem.PORT);
        // final ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        // out.writeObject((Object)msg);
        // final BufferedReader datafromserver = new BufferedReader((Reader)new InputStreamReader(socket.getInputStream()));
        String fromServer;
        // while ((fromServer = datafromserver.readLine()) != null && !fromServer.equals("done.")) {}
        // out.flush();
        // socket.close();
        return true;
    }
}
