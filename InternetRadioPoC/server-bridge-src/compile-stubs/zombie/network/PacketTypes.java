package zombie.network;

import zombie.core.network.ByteBufferWriter;

public final class PacketTypes {
    public static final class PacketType {
        public static final PacketType SyncRadioData = null;
        public void doPacket(ByteBufferWriter writer) { }
        public void send(IConnection connection) { }
    }
}
