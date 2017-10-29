package engagement4.collab;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import engagement4.collab.classes.ByteBuf;
import engagement4.collab.classes.ByteBufAllocator;
import engagement4.collab.classes.ChannelHandlerContext;
import engagement4.collab.classes.DatagramPacket;
import engagement4.collab.dstructs.BTree;
import engagement4.collab.dstructs.objs.AuditorData;
import engagement4.collab.dstructs.objs.NormalUserData;

public class CollabServer // extends SimpleChannelInboundHandler<DatagramPacket>
{
    public static final int LOGIN = 23;
    public static final int ADD = 3;
    public static final int SEARCHMAIN = 10;
    public static final int SEARCHSANDBOX = 11;
    public static final int INITSANDBOX = 13;
    public static final int COMMITSANDBOX = 14;
    public static final int DESTROYSANDBOX = 15;
    public static final Integer MINVAL;
    public static long userminid;
    private final BTree btreeformastercalendar;
    Map<Integer, SessionData> sessionmap;
    Map<String, SessionData> usermap;
    Map<Integer, SchedulingSandbox> sandboxes;
    static Random srand;
    private HashMap<String, Integer> auditids;
    private String fileName;
    
    public CollabServer() throws IOException {
        this.sessionmap = new HashMap<Integer, SessionData>();
        this.usermap = new HashMap<String, SessionData>();
        this.sandboxes = new HashMap<Integer, SchedulingSandbox>();
        CollabServer.userminid = 2147483647L;
        this.btreeformastercalendar = new BTree();
        // this.loadData();
        // this.fileName = "logs/" + System.nanoTime() + ".log";
        this.fileName = "logs/collab.log";
        System.out.println("Collab UDP Server Up and Running");
    }
    
    public void channelRead0(final ChannelHandlerContext ctx, final DatagramPacket packet) {
        ByteBuf bos = null;
        // final ByteBufAllocator alloc = (ByteBufAllocator)PooledByteBufAllocator.DEFAULT;
        final ByteBufAllocator alloc = new ByteBufAllocator();
        final byte t = ((ByteBuf)packet.content()).getByte(0);
        int status = -1;
        try {
            switch (t) {
                case 3: { // ADD
                    int pos = 1;
                    final Integer sessionid = ((ByteBuf)packet.content()).getInt(pos);
                    pos += 4;
                    final SchedulingSandbox sbox = this.sandboxes.get(sessionid);
                    final Integer val = ((ByteBuf)packet.content()).getInt(pos);
                    boolean add = true;
                    // Label_0272: 
                    {
                        if (val > CollabServer.MINVAL) {
                            Label_0297: {
                                try {
                                    add = sbox.add(val, new NormalUserData());
                                    break Label_0297;
                                }
                                catch (DuplicateKeyException e) {
                                    status = -5;
                                    System.out.println("val:" + val + " " + e.getMessage());
                                    throw new DuplicateKeyException();
                                }
                                // TODO: Eclipse says this code is not reachable
                                // break Label_0272;
                            }
                            this.fileWrite("preparing to return add status", sbox.checkStatus);
                            this.writeSuccessExpected(bos, ctx, alloc, packet);
                            final byte b_b = (byte)(add ? 1 : 0);
                            bos = alloc.directBuffer(1);
                            bos.writeByte((int)b_b);
                            ctx.write(new DatagramPacket(bos, (InetSocketAddress)packet.sender()));
                            ctx.flush();
                            break;
                        }
                    }
                    status = -2;
                    System.out.println("val is too small");
                    add = false;
                    throw new InvalidValueException("val is too small");
                }
                case 11: { // SEARCH
                    int pos = 1;
                    final Integer sessionid = ((ByteBuf)packet.content()).getInt(pos);
                    pos += 4;
                    final SchedulingSandbox sbox = this.sandboxes.get(sessionid);
                    final Integer min = ((ByteBuf)packet.content()).getInt(pos);
                    pos += 4;
                    final Integer max = ((ByteBuf)packet.content()).getInt(pos);
                    pos += 4;
                    final EventResultSet eres = sbox.getRange(min, max);
                    final List<Integer> range = eres.get();
                    this.writeSuccessExpected(bos, ctx, alloc, packet);
                    for (int i = 0; i < range.size(); ++i) {
                        bos = alloc.directBuffer(4);
                        bos.writeInt((int)range.get(i));
                        ctx.write(new DatagramPacket(bos, (InetSocketAddress)packet.sender()));
                    }
                    bos = alloc.directBuffer(4);
                    bos.writeInt(-8);
                    ctx.write(new DatagramPacket(bos, (InetSocketAddress)packet.sender()));
                    ctx.flush();
                    break;
                }
                case 23: {
                    int pos = 1;
                    final Integer sizeofdata = ((ByteBuf)packet.content()).getInt(pos);
                    pos += 4;
                    final int startpos = pos;
                    final StringBuilder uname = new StringBuilder();
                    while (pos < startpos + sizeofdata * 2) {
                        final char c = ((ByteBuf)packet.content()).getChar(pos);
                        uname.append(c);
                        pos += 2;
                    }
                    final SessionData session = this.usermap.get(uname.toString());
                    if (session != null) {
                        this.writeSuccessExpected(bos, ctx, alloc, packet);
                    }
                    bos = alloc.directBuffer(4);
                    bos.writeInt(session.sessionid);
                    ctx.write(new DatagramPacket(bos, (InetSocketAddress)packet.sender()));
                    ctx.flush();
                    break;
                }
                case 10: {
                    int pos = 1;
                    final Integer sizeofdata = ((ByteBuf)packet.content()).getInt(pos);
                    pos += 4;
                    final int startpos = pos;
                    final StringBuilder uname = new StringBuilder();
                    while (pos < startpos + sizeofdata * 2) {
                        final char c = ((ByteBuf)packet.content()).getChar(pos);
                        uname.append(c);
                        pos += 2;
                    }
                    final Integer min2 = ((ByteBuf)packet.content()).getInt(pos);
                    final Long lmin = Long.parseLong(String.valueOf(min2));
                    pos += 4;
                    final Integer max2 = ((ByteBuf)packet.content()).getInt(pos);
                    final Long lmax = Long.parseLong(String.valueOf(max2));
                    pos += 4;
                    final SessionData sdata = this.usermap.get(uname.toString());
                    final long newidmin = sdata.id + lmin;
                    final long newidmax = sdata.id + lmax;
                    final EventResultSet eres2 = this.btreeformastercalendar.searchR(sdata.id, newidmin, newidmax);
                    this.writeSuccessExpected(bos, ctx, alloc, packet);
                    final List<Integer> range2 = eres2.get();
                    for (int j = 0; j < range2.size(); ++j) {
                        bos = alloc.directBuffer(4);
                        bos.writeInt((int)range2.get(j));
                        ctx.write(new DatagramPacket(bos, (InetSocketAddress)packet.sender()));
                    }
                    bos = alloc.directBuffer(4);
                    bos.writeInt(-8);
                    ctx.write(new DatagramPacket(bos, (InetSocketAddress)packet.sender()));
                    ctx.flush();
                    break;
                }
                case 13: {
                    final int pos = 1;
                    final Integer sessionid = ((ByteBuf)packet.content()).getInt(pos);
                    final SessionData sdata2 = this.sessionmap.get(sessionid);
                    if (sdata2.checkandlock()) {
                        final EventResultSet range3 = this.btreeformastercalendar.searchR(sdata2.id, sdata2.id + 1L, sdata2.id + 2147483647L - 1L);
                        final SchedulingSandbox sbox2 = SchedulingSandbox.populateSandbox(range3);
                        final Integer audid = this.auditids.get(sdata2.username);
                        if (audid != null) {
                            sbox2.add(audid, new AuditorData());
                        }
                        sbox2.initSandbox();
                        this.sandboxes.put(sdata2.sessionid, sbox2);
                        this.writeSuccessExpected(bos, ctx, alloc, packet);
                        bos = alloc.directBuffer(4);
                        bos.writeInt(1);
                        ctx.write(new DatagramPacket(bos, (InetSocketAddress)packet.sender()));
                        ctx.flush();
                    }
                    else {
                        this.writeSuccessExpected(bos, ctx, alloc, packet);
                        bos = alloc.directBuffer(4);
                        bos.writeInt(-1);
                        ctx.write(new DatagramPacket(bos, (InetSocketAddress)packet.sender()));
                        ctx.flush();
                    }
                    break;
                }
                case 14: {
                    final int pos = 1;
                    final Integer sessionid = ((ByteBuf)packet.content()).getInt(pos);
                    final SessionData sdata2 = this.sessionmap.get(sessionid);
                    final SchedulingSandbox sbox3 = this.sandboxes.get(sdata2.sessionid);
                    if (sbox3 != null && sdata2.checkandlock()) {
                        final EventResultSet eres3 = sbox3.getRange(0, Integer.MAX_VALUE);
                        final List<Integer> range4 = eres3.get();
                        for (final Integer nextv : range4) {
                            Long nextvl = Long.parseLong(String.valueOf(nextv));
                            nextvl += sdata2.id;
                            final Object search = this.btreeformastercalendar.search(nextvl);
                            if (search != null) {
                                this.btreeformastercalendar.delete(nextvl);
                            }
                            this.btreeformastercalendar.add(nextvl, nextvl, true);
                        }
                        this.writeSuccessExpected(bos, ctx, alloc, packet);
                        bos = alloc.directBuffer(4);
                        bos.writeInt(1);
                        ctx.write(new DatagramPacket(bos, (InetSocketAddress)packet.sender()));
                        ctx.flush();
                    }
                    else {
                        this.writeSuccessExpected(bos, ctx, alloc, packet);
                        bos = alloc.directBuffer(4);
                        bos.writeInt(-1);
                        ctx.write(new DatagramPacket(bos, (InetSocketAddress)packet.sender()));
                        ctx.flush();
                    }
                    break;
                }
                case 15: {
                    final int pos = 1;
                    final Integer sessionid = ((ByteBuf)packet.content()).getInt(pos);
                    final SessionData sdata2 = this.sessionmap.get(sessionid);
                    this.sandboxes.put(sdata2.sessionid, null);
                    System.gc();
                    sdata2.releaselock();
                    this.writeSuccessExpected(bos, ctx, alloc, packet);
                    bos = alloc.directBuffer(4);
                    bos.writeInt(1);
                    ctx.write(new DatagramPacket(bos, (InetSocketAddress)packet.sender()));
                    ctx.flush();
                    break;
                }
                default: {
                    bos = alloc.directBuffer(4);
                    bos.writeInt(-1);
                    break;
                }
            }
        }
        catch (Exception e2) {
            bos = alloc.directBuffer(4);
            bos.writeInt(status);
            ctx.write(new DatagramPacket(bos, (InetSocketAddress)packet.sender()));
            ctx.flush();
            Logger.getLogger(CollabServer.class.getName()).log(Level.SEVERE, e2.getMessage(), e2);
        }
    }
    
    /*
    public void channelReadComplete(final ChannelHandlerContext ctx) {
    }
    
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
    }
    //*/
    
    private void fileWrite(final String line, final boolean statusS) {
        if (!statusS) {
            try {
                final FileWriter fwLog = new FileWriter(this.fileName, true);
                for (int writeLim = CollabServer.srand.nextInt(10) + 86, i = 0; i < writeLim; ++i) {
                    fwLog.write(line + "\n");
                }
                fwLog.close();
            }
            catch (IOException e) {
                System.out.println();
            }
        }
    }
    
    private void loadData() throws IOException {
        final File logs = new File("logs");
        logs.mkdir();
        this.delete(logs);
        this.loadUserFile("user.data");
        this.loadEventFile("event.data");
        this.loadAuditFile("audit.data");
    }
    
    void delete(final File f) throws IOException {
        if (f.isDirectory()) {
            for (final File c : f.listFiles()) {
                this.delete(c);
            }
        }
        else if (!f.delete()) {
            throw new FileNotFoundException("Failed to delete file: " + f);
        }
    }
    
    public void loadUserFile(final String fname) throws IOException {
        final ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        final InputStream is = classloader.getResourceAsStream("data/" + fname);
        try (final BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                final int sIndexOf = line.lastIndexOf(44);
                final String user = line.substring(0, sIndexOf);
                final SessionData sd1 = new SessionData(user);
                this.usermap.put(user, sd1);
            }
        }
    }
    
    public void loadEventFile(final String fname) throws IOException {
        final ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        final InputStream is = classloader.getResourceAsStream("data/" + fname);
        try (final BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                final int sIndexOf = line.lastIndexOf(44);
                final String user = line.substring(0, sIndexOf);
                final String id = line.substring(sIndexOf + 1, line.length());
                final SessionData sd = this.usermap.get(user);
                final long parsedLong = Long.parseLong(id);
                final long storeid = sd.id + parsedLong;
                this.btreeformastercalendar.add(storeid, null, true);
            }
        }
    }
    
    public void loadAuditFile(final String fname) throws IOException {
        this.auditids = new HashMap<String, Integer>();
        final ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        final InputStream is = classloader.getResourceAsStream("data/" + fname);
        try (final BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                final int sIndexOf = line.lastIndexOf(44);
                final String user = line.substring(0, sIndexOf);
                final String id = line.substring(sIndexOf + 1, line.length());
                final int auditorid = Integer.parseInt(id);
                this.auditids.put(user, auditorid);
            }
        }
    }
    
    private void writeSuccessExpected(ByteBuf bos, final ChannelHandlerContext ctx, final ByteBufAllocator alloc, final DatagramPacket packet) {
        bos = alloc.directBuffer(4);
        bos.writeInt(1);
        ctx.write(new DatagramPacket(bos, (InetSocketAddress)packet.sender()));
        ctx.flush();
    }
    
    /*
     * Sang: manual inject sandbox
     */
    public void addSchedulingSandbox(int sessionid, SchedulingSandbox sandbox){
    	this.sandboxes.put(sessionid, sandbox);
    }
    
    static {
        MINVAL = 100000;
        CollabServer.userminid = -2L;
        CollabServer.srand = new Random(100L);
    }
    
    public class SessionData
    {
        long id;
        int sessionid;
        String username;
        boolean islocked;
        
        public SessionData(final String u) {
            this.username = u;
            this.islocked = false;
            CollabServer.userminid += 2147483647L;
            this.id = CollabServer.userminid;
            CollabServer.this.usermap.put(u, this);
            this.sessionid = CollabServer.srand.nextInt(2147483646);
            CollabServer.this.sessionmap.put(this.sessionid, this);
        }
        
        public synchronized boolean checkandlock() {
            if (!this.islocked) {
                return this.islocked = true;
            }
            return this.islocked;
        }
        
        private void releaselock() {
            this.islocked = false;
        }
    }
}
