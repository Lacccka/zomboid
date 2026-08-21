/*
 * Decompiled with CFR 0.152.
 */
package zombie.network;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.LambdaMetafactory;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import org.lwjglx.BufferUtils;
import zombie.ChunkMapFilenames;
import zombie.core.logger.LoggerManager;
import zombie.core.math.PZMath;
import zombie.core.network.ByteBufferWriter;
import zombie.core.raknet.UdpConnection;
import zombie.core.random.Rand;
import zombie.debug.DebugType;
import zombie.debug.LogSeverity;
import zombie.iso.IsoChunk;
import zombie.iso.IsoWorld;
import zombie.network.ChunkChecksum;
import zombie.network.ClientChunkRequest;
import zombie.network.PacketTypes;
import zombie.network.ServerMap;
import zombie.network.packets.INetworkPacket;
import zombie.network.packets.SentChunkPacket;

public final class PlayerDownloadServer {
    private static final long CHUNK_GENERATION_TIMEOUT_MS = 30000L;
    private static final long OUT_OF_RANGE_GRACE_MS = 5000L;
    private static final int OUT_OF_RANGE_GRID_FACTOR = 2;
    private static final int MAX_PENDING_CHUNKS = 4096;
    private static final int MAX_OUT_OF_RANGE_REQUESTS = 1024;
    public WorkerThread workerThread;
    private final UdpConnection connection;
    private boolean networkFileDebug;
    private final CRC32 crc32 = new CRC32();
    private final ByteBuffer bb = ByteBuffer.allocate(1000000);
    private final ByteBuffer sb = BufferUtils.createByteBuffer(1000000);
    private final ByteBufferWriter bbw = new ByteBufferWriter(this.bb);
    private final ConcurrentLinkedQueue<QueuedRequest> queuedByWorker = new ConcurrentLinkedQueue();
    private final List<OutOfRangeRequest> outOfRangeRequests = new ArrayList<OutOfRangeRequest>();
    private final LinkedHashMap<Integer, PendingChunk> pendingChunks = new LinkedHashMap();
    public final List<ClientChunkRequest> ccrWaiting = new ArrayList<ClientChunkRequest>();

    public PlayerDownloadServer(UdpConnection connection) {
        this.connection = connection;
        this.workerThread = new WorkerThread(this);
        this.workerThread.setDaemon(true);
        this.workerThread.setName("PlayerDownloadServer" + Rand.Next(Integer.MAX_VALUE));
        this.workerThread.start();
    }

    public void destroy() {
        this.workerThread.putCommand(EThreadCommand.Quit, null);
        while (this.workerThread.isAlive()) {
            try {
                Thread.sleep(10L);
            }
            catch (InterruptedException interruptedException) {}
        }
        this.workerThread = null;
    }

    private static int chunkKey(int wx, int wy) {
        return wx << 16 | wy & 0xFFFF;
    }

    void queueUntilGenerated(int requestNumber, int wx, int wy) {
        this.queuedByWorker.add(new QueuedRequest(requestNumber, wx, wy));
    }

    private boolean isRequestInRange(int wx, int wy) {
        int gridWidth = this.connection.getChunkGridWidth();
        if (gridWidth <= 0) {
            return true;
        }
        float x = (float)(wx * 8) + 4.0f;
        float y = (float)(wy * 8) + 4.0f;
        float radius = (float)gridWidth * 2.0f * 8.0f;
        return this.connection.RelevantTo(x, y, radius);
    }

    private void addPendingChunk(QueuedRequest queued) {
        if (this.pendingChunks.size() >= 4096) {
            INetworkPacket.send(this.connection, PacketTypes.PacketType.ChunkNotReady, queued.requestNumber);
            return;
        }
        this.pendingChunks.computeIfAbsent((Integer)Integer.valueOf((int)PlayerDownloadServer.chunkKey((int)queued.wx, (int)queued.wy)), (Function<Integer, PendingChunk>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Ljava/lang/Object;, lambda$addPendingChunk$0(zombie.network.PlayerDownloadServer$QueuedRequest java.lang.Integer ), (Ljava/lang/Integer;)Lzombie/network/PlayerDownloadServer$PendingChunk;)((QueuedRequest)queued)).requestNumbers.add(queued.requestNumber);
    }

    private void updateOutOfRangeRequests() {
        if (this.outOfRangeRequests.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        for (int i = 0; i < this.outOfRangeRequests.size(); ++i) {
            OutOfRangeRequest held = this.outOfRangeRequests.get(i);
            if (this.workerThread.isRequestCancelled(held.request.requestNumber)) {
                this.outOfRangeRequests.remove(i--);
                continue;
            }
            if (this.isRequestInRange(held.request.wx, held.request.wy)) {
                this.outOfRangeRequests.remove(i--);
                this.addPendingChunk(held.request);
                continue;
            }
            if (now - held.receivedMs <= 5000L) continue;
            DebugType.Multiplayer.warn("chunk request %d,%d stayed outside the streamed area of %s", held.request.wx, held.request.wy, this.connection.getUserName());
            this.outOfRangeRequests.remove(i--);
            this.addPendingChunk(held.request);
        }
    }

    private void updatePendingChunks() {
        QueuedRequest queued = this.queuedByWorker.poll();
        while (queued != null) {
            int wx = queued.wx;
            int wy = queued.wy;
            if (!IsoWorld.instance.getMetaGrid().isValidChunk(wx, wy)) {
                this.workerThread.sendNotRequired(queued.requestNumber, true);
            } else if (!this.isRequestInRange(wx, wy)) {
                if (this.outOfRangeRequests.size() >= 1024) {
                    OutOfRangeRequest oldest = this.outOfRangeRequests.remove(0);
                    INetworkPacket.send(this.connection, PacketTypes.PacketType.ChunkNotReady, oldest.request.requestNumber);
                }
                this.outOfRangeRequests.add(new OutOfRangeRequest(queued, System.currentTimeMillis()));
            } else {
                this.addPendingChunk(queued);
            }
            queued = this.queuedByWorker.poll();
        }
        this.updateOutOfRangeRequests();
        if (this.pendingChunks.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        ClientChunkRequest readyRequest = null;
        Iterator<Map.Entry<Integer, PendingChunk>> iterator2 = this.pendingChunks.entrySet().iterator();
        while (iterator2.hasNext()) {
            PendingChunk pendingChunk = iterator2.next().getValue();
            for (int i = 0; i < pendingChunk.requestNumbers.size(); ++i) {
                if (!this.workerThread.isRequestCancelled(pendingChunk.requestNumbers.get(i))) continue;
                pendingChunk.requestNumbers.remove(i--);
            }
            if (pendingChunk.requestNumbers.isEmpty()) {
                iterator2.remove();
                continue;
            }
            ServerMap.instance.loadOrKeepRelevent(PZMath.coorddivision(pendingChunk.wx, 8) - ServerMap.instance.getMinX(), PZMath.coorddivision(pendingChunk.wy, 8) - ServerMap.instance.getMinY());
            IsoChunk serverChunk = ServerMap.instance.getChunk(pendingChunk.wx, pendingChunk.wy);
            if (serverChunk != null && serverChunk.loaded) {
                for (int i = 0; i < pendingChunk.requestNumbers.size(); ++i) {
                    if (readyRequest == null || readyRequest.isChunksFilled()) {
                        if (readyRequest != null) {
                            this.ccrWaiting.add(readyRequest);
                        }
                        readyRequest = this.getClientChunkRequest(false);
                    }
                    ClientChunkRequest.Chunk reqChunk = readyRequest.getChunk();
                    reqChunk.requestNumber = pendingChunk.requestNumbers.get(i);
                    reqChunk.wx = pendingChunk.wx;
                    reqChunk.wy = pendingChunk.wy;
                    reqChunk.crc = 0L;
                    readyRequest.chunks.add(reqChunk);
                }
                iterator2.remove();
                continue;
            }
            if (now - pendingChunk.firstRequestedMs <= 30000L) continue;
            DebugType.Multiplayer.warn("the chunk %d,%d was not generated in %d ms", pendingChunk.wx, pendingChunk.wy, now - pendingChunk.firstRequestedMs);
            INetworkPacket.send(this.connection, PacketTypes.PacketType.ChunkNotReady, pendingChunk.requestNumbers.toArray());
            iterator2.remove();
        }
        if (readyRequest != null) {
            this.ccrWaiting.add(readyRequest);
        }
    }

    public ClientChunkRequest getClientChunkRequest(boolean isLargeArea) {
        ClientChunkRequest ccr = this.workerThread.freeRequests.poll();
        if (ccr == null) {
            ccr = new ClientChunkRequest();
        }
        ccr.largeArea = isLargeArea;
        return ccr;
    }

    public final int getWaitingRequests() {
        return this.ccrWaiting.size();
    }

    public void update() {
        this.networkFileDebug = DebugType.NetworkFileDebug.isEnabled();
        if (!this.workerThread.ready) {
            return;
        }
        this.updatePendingChunks();
        this.removeOlderDuplicateRequests();
        if (this.ccrWaiting.isEmpty()) {
            if (this.workerThread.cancelQ.isEmpty() && !this.workerThread.cancelled.isEmpty()) {
                this.workerThread.cancelled.clear();
            }
            return;
        }
        ClientChunkRequest ccr = this.ccrWaiting.remove(0);
        for (int i = 0; i < ccr.chunks.size(); ++i) {
            ClientChunkRequest.Chunk reqChunk = ccr.chunks.get(i);
            if (this.workerThread.isRequestCancelled(reqChunk.requestNumber)) {
                ccr.chunks.remove(i--);
                ccr.releaseChunk(reqChunk);
                continue;
            }
            IsoChunk chunk = ServerMap.instance.getChunk(reqChunk.wx, reqChunk.wy);
            if (chunk == null || !chunk.loaded) continue;
            try {
                ccr.getByteBuffer(reqChunk);
                chunk.SaveLoadedChunk(reqChunk, this.crc32);
                continue;
            }
            catch (Exception ex) {
                DebugType.General.printException(ex, LogSeverity.Error);
                LoggerManager.getLogger("map").write(ex);
                this.workerThread.sendNotRequired(reqChunk.requestNumber, false);
                ccr.chunks.remove(i--);
                ccr.releaseChunk(reqChunk);
            }
        }
        if (ccr.chunks.isEmpty()) {
            this.workerThread.freeRequests.add(ccr);
            return;
        }
        this.workerThread.ready = false;
        this.workerThread.putCommand(EThreadCommand.RequestZipArray, ccr);
    }

    private void removeOlderDuplicateRequests() {
        for (int i = this.ccrWaiting.size() - 1; i >= 0; --i) {
            ClientChunkRequest ccr1 = this.ccrWaiting.get(i);
            for (int j = 0; j < ccr1.chunks.size(); ++j) {
                ClientChunkRequest.Chunk chunk1 = ccr1.chunks.get(j);
                if (this.workerThread.isRequestCancelled(chunk1.requestNumber)) {
                    ccr1.chunks.remove(j--);
                    ccr1.releaseChunk(chunk1);
                    continue;
                }
                for (int k = i - 1; k >= 0; --k) {
                    ClientChunkRequest ccr2 = this.ccrWaiting.get(k);
                    if (!this.cancelDuplicateChunk(ccr2, chunk1.wx, chunk1.wy)) continue;
                }
            }
            if (!ccr1.chunks.isEmpty()) continue;
            this.ccrWaiting.remove(i);
            this.workerThread.freeRequests.add(ccr1);
        }
    }

    private boolean cancelDuplicateChunk(ClientChunkRequest ccr, int wx, int wy) {
        for (int i = 0; i < ccr.chunks.size(); ++i) {
            ClientChunkRequest.Chunk reqChunk = ccr.chunks.get(i);
            if (this.workerThread.isRequestCancelled(reqChunk.requestNumber)) {
                ccr.chunks.remove(i--);
                ccr.releaseChunk(reqChunk);
                continue;
            }
            if (reqChunk.wx != wx || reqChunk.wy != wy) continue;
            this.workerThread.sendNotRequired(reqChunk.requestNumber, false);
            ccr.chunks.remove(i);
            ccr.releaseChunk(reqChunk);
            return true;
        }
        return false;
    }

    private void sendPacket(PacketTypes.PacketType packetType) {
        this.bb.flip();
        this.sb.put(this.bb);
        this.sb.flip();
        this.connection.getPeer().SendRaw(this.sb, packetType.packetPriority, packetType.packetReliability, (byte)0, this.connection.getConnectedGUID(), false);
        this.sb.clear();
    }

    private ByteBufferWriter startPacket() {
        this.bb.clear();
        return this.bbw;
    }

    private static /* synthetic */ PendingChunk lambda$addPendingChunk$0(QueuedRequest queued, Integer key) {
        return new PendingChunk(queued.wx, queued.wy, System.currentTimeMillis());
    }

    public final class WorkerThread
    extends Thread {
        boolean quit;
        volatile boolean ready;
        final LinkedBlockingQueue<WorkerThreadCommand> commandQ;
        final ConcurrentLinkedQueue<ClientChunkRequest> freeRequests;
        public final ConcurrentLinkedQueue<Integer> cancelQ;
        final HashSet<Integer> cancelled;
        final CRC32 crcMaker;
        byte[] inMemoryZip;
        final Deflater compressor;
        final /* synthetic */ PlayerDownloadServer this$0;

        public WorkerThread(PlayerDownloadServer this$0) {
            PlayerDownloadServer playerDownloadServer = this$0;
            Objects.requireNonNull(playerDownloadServer);
            this.this$0 = playerDownloadServer;
            this.ready = true;
            this.commandQ = new LinkedBlockingQueue();
            this.freeRequests = new ConcurrentLinkedQueue();
            this.cancelQ = new ConcurrentLinkedQueue();
            this.cancelled = new HashSet();
            this.crcMaker = new CRC32();
            this.inMemoryZip = new byte[20480];
            this.compressor = new Deflater();
        }

        @Override
        public void run() {
            while (!this.quit) {
                try {
                    this.runInner();
                }
                catch (Exception ex) {
                    DebugType.General.printException(ex, LogSeverity.Error);
                }
            }
        }

        private void runInner() throws InterruptedException, IOException {
            WorkerThreadCommand command = this.commandQ.take();
            switch (command.e.ordinal()) {
                case 0: {
                    try {
                        this.sendLargeArea(command.ccr);
                        break;
                    }
                    finally {
                        this.ready = true;
                    }
                }
                case 1: {
                    try {
                        this.sendArray(command.ccr);
                        break;
                    }
                    finally {
                        this.ready = true;
                    }
                }
                case 2: {
                    this.quit = true;
                }
            }
        }

        void putCommand(EThreadCommand e, ClientChunkRequest ccr) {
            WorkerThreadCommand command = new WorkerThreadCommand();
            command.e = e;
            command.ccr = ccr;
            while (true) {
                try {
                    this.commandQ.put(command);
                }
                catch (InterruptedException interruptedException) {
                    continue;
                }
                break;
            }
        }

        public int compressChunk(ClientChunkRequest.Chunk chunk) {
            this.compressor.reset();
            this.compressor.setInput(chunk.bb.array(), 0, chunk.bb.limit());
            this.compressor.finish();
            if ((double)this.inMemoryZip.length < (double)chunk.bb.limit() * 1.5) {
                this.inMemoryZip = new byte[(int)((double)chunk.bb.limit() * 1.5)];
            }
            return this.compressor.deflate(this.inMemoryZip, 0, this.inMemoryZip.length, 3);
        }

        private void sendChunk(ClientChunkRequest.Chunk chunk) {
            try {
                SentChunkPacket packet = new SentChunkPacket();
                int filesize = this.compressChunk(chunk);
                packet.setChunk(chunk, filesize, this.inMemoryZip);
                while (packet.hasData()) {
                    ByteBufferWriter b = this.this$0.startPacket();
                    PacketTypes.PacketType.SentChunk.doPacket(b);
                    packet.write(b);
                    this.this$0.sendPacket(PacketTypes.PacketType.SentChunk);
                }
            }
            catch (Exception ex) {
                DebugType.Multiplayer.printException(ex, "sendChunk error", LogSeverity.Error);
                this.sendNotRequired(chunk.requestNumber, false);
            }
        }

        private void sendNotRequired(int requestNumber, boolean sameOnServer) {
            ByteBufferWriter b = this.this$0.startPacket();
            PacketTypes.PacketType.NotRequiredInZip.doPacket(b);
            b.putInt(1);
            b.putInt(requestNumber);
            b.putBoolean(sameOnServer);
            this.this$0.sendPacket(PacketTypes.PacketType.NotRequiredInZip);
        }

        private void sendLargeArea(ClientChunkRequest ccr) throws IOException {
            for (int n = 0; n < ccr.chunks.size(); ++n) {
                ClientChunkRequest.Chunk reqChunk = ccr.chunks.get(n);
                int wx = reqChunk.wx;
                int wy = reqChunk.wy;
                if (reqChunk.bb != null) {
                    reqChunk.bb.limit(reqChunk.bb.position());
                    reqChunk.bb.position(0);
                    this.sendChunk(reqChunk);
                    ccr.releaseBuffer(reqChunk);
                    continue;
                }
                File inFile = ChunkMapFilenames.instance.getFilename(wx, wy);
                if (!inFile.exists()) continue;
                ccr.getByteBuffer(reqChunk);
                reqChunk.bb = IsoChunk.SafeRead(wx, wy, reqChunk.bb);
                this.sendChunk(reqChunk);
                ccr.releaseBuffer(reqChunk);
            }
            ClientChunkRequest.freeBuffers.clear();
            ccr.chunks.clear();
        }

        private void sendArray(ClientChunkRequest ccr) throws IOException {
            int n;
            for (n = 0; n < ccr.chunks.size(); ++n) {
                ClientChunkRequest.Chunk reqChunk = ccr.chunks.get(n);
                if (this.isRequestCancelled(reqChunk.requestNumber)) continue;
                int wx = reqChunk.wx;
                int wy = reqChunk.wy;
                if (reqChunk.bb != null) {
                    boolean add = true;
                    if (reqChunk.crc != 0L) {
                        this.crcMaker.reset();
                        this.crcMaker.update(reqChunk.bb.array(), 0, reqChunk.bb.position());
                        boolean bl = add = reqChunk.crc != this.crcMaker.getValue();
                        if (add && this.this$0.networkFileDebug) {
                            DebugType.NetworkFileDebug.debugln(wx + "," + wy + ": crc server=" + this.crcMaker.getValue() + " client=" + reqChunk.crc);
                        }
                    }
                    if (add) {
                        if (this.this$0.networkFileDebug) {
                            DebugType.NetworkFileDebug.debugln(wx + "," + wy + ": send=true loaded=true");
                        }
                        reqChunk.bb.limit(reqChunk.bb.position());
                        reqChunk.bb.position(0);
                        this.sendChunk(reqChunk);
                    } else {
                        if (this.this$0.networkFileDebug) {
                            DebugType.NetworkFileDebug.debugln(wx + "," + wy + ": send=false loaded=true");
                        }
                        this.sendNotRequired(reqChunk.requestNumber, true);
                    }
                    ccr.releaseBuffer(reqChunk);
                    continue;
                }
                File inFile = ChunkMapFilenames.instance.getFilename(wx, wy);
                if (inFile.exists()) {
                    long crcCached = ChunkChecksum.getChecksum(wx, wy);
                    if (crcCached != 0L && crcCached == reqChunk.crc) {
                        if (this.this$0.networkFileDebug) {
                            DebugType.NetworkFileDebug.debugln(wx + "," + wy + ": send=false loaded=false file=true");
                        }
                        this.sendNotRequired(reqChunk.requestNumber, true);
                        continue;
                    }
                    ccr.getByteBuffer(reqChunk);
                    reqChunk.bb = IsoChunk.SafeRead(wx, wy, reqChunk.bb);
                    boolean add = true;
                    if (reqChunk.crc != 0L) {
                        this.crcMaker.reset();
                        this.crcMaker.update(reqChunk.bb.array(), 0, reqChunk.bb.limit());
                        boolean bl = add = reqChunk.crc != this.crcMaker.getValue();
                    }
                    if (add) {
                        if (this.this$0.networkFileDebug) {
                            DebugType.NetworkFileDebug.debugln(wx + "," + wy + ": send=true loaded=false file=true");
                        }
                        this.sendChunk(reqChunk);
                    } else {
                        if (this.this$0.networkFileDebug) {
                            DebugType.NetworkFileDebug.debugln(wx + "," + wy + ": send=false loaded=false file=true");
                        }
                        this.sendNotRequired(reqChunk.requestNumber, true);
                    }
                    ccr.releaseBuffer(reqChunk);
                    continue;
                }
                if (this.this$0.networkFileDebug) {
                    DebugType.NetworkFileDebug.debugln(wx + "," + wy + ": send=false loaded=false file=false");
                }
                this.this$0.queueUntilGenerated(reqChunk.requestNumber, wx, wy);
            }
            for (n = 0; n < ccr.chunks.size(); ++n) {
                ccr.releaseChunk(ccr.chunks.get(n));
            }
            ccr.chunks.clear();
            this.freeRequests.add(ccr);
        }

        private boolean isRequestCancelled(int requestNumber) {
            Integer cancelledNumber = this.cancelQ.poll();
            while (cancelledNumber != null) {
                this.cancelled.add(cancelledNumber);
                cancelledNumber = this.cancelQ.poll();
            }
            if (this.cancelled.remove(requestNumber)) {
                if (this.this$0.networkFileDebug) {
                    DebugType.NetworkFileDebug.debugln("cancelled request #" + requestNumber);
                }
                return true;
            }
            return false;
        }
    }

    private static enum EThreadCommand {
        RequestLargeArea,
        RequestZipArray,
        Quit;

    }

    private static final class QueuedRequest {
        final int requestNumber;
        final int wx;
        final int wy;

        QueuedRequest(int requestNumber, int wx, int wy) {
            this.requestNumber = requestNumber;
            this.wx = wx;
            this.wy = wy;
        }
    }

    private static final class PendingChunk {
        final int wx;
        final int wy;
        final long firstRequestedMs;
        final List<Integer> requestNumbers = new ArrayList<Integer>();

        PendingChunk(int wx, int wy, long firstRequestedMs) {
            this.wx = wx;
            this.wy = wy;
            this.firstRequestedMs = firstRequestedMs;
        }
    }

    private static final class OutOfRangeRequest {
        final QueuedRequest request;
        final long receivedMs;

        OutOfRangeRequest(QueuedRequest request, long receivedMs) {
            this.request = request;
            this.receivedMs = receivedMs;
        }
    }

    private static final class WorkerThreadCommand {
        EThreadCommand e;
        ClientChunkRequest ccr;

        private WorkerThreadCommand() {
        }
    }
}

