package zombie.core.raknet;

import zombie.characters.IsoPlayer;
import zombie.core.network.ByteBufferWriter;
import zombie.network.IConnection;

public class UdpConnection implements IConnection {
    public IsoPlayer[] players;
    public short[] playerIds;
    public boolean isFullyConnected() { return false; }
    public long getConnectedGUID() { return 0L; }
    public ByteBufferWriter startPacket() { return null; }
}
