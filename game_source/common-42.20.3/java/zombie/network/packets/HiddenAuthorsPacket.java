/*
 * Decompiled with CFR 0.152.
 */
package zombie.network.packets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import zombie.characters.Capability;
import zombie.core.network.ByteBufferReader;
import zombie.core.network.ByteBufferWriter;
import zombie.core.raknet.UdpConnection;
import zombie.network.GameServer;
import zombie.network.IConnection;
import zombie.network.JSONField;
import zombie.network.PacketSetting;
import zombie.network.PacketTypes;
import zombie.network.packets.INetworkPacket;
import zombie.worldMap.network.HiddenAuthors;

@PacketSetting(ordering=0, priority=1, reliability=3, requiredCapability=Capability.LoginOnServer, handlingType=3)
public class HiddenAuthorsPacket
implements INetworkPacket {
    public static final int MAX_USER_NAMES = 127;
    @JSONField
    private boolean hidden;
    @JSONField
    private final List<String> userNames = new ArrayList<String>();

    @Override
    public void setData(Object ... values2) {
        this.hidden = (Boolean)values2[0];
        this.userNames.clear();
        if (values2[1] instanceof String) {
            this.userNames.add((String)values2[1]);
            return;
        }
        if (!(values2[1] instanceof Collection) || ((Collection)values2[1]).size() > 127) {
            throw new IllegalArgumentException("Invalid userNames array");
        }
        this.userNames.addAll((Collection)values2[1]);
    }

    @Override
    public void parse(ByteBufferReader b, IConnection connection) {
        this.hidden = b.getBoolean();
        this.userNames.clear();
        int count = b.getByte();
        for (int i = 0; i < count; ++i) {
            this.userNames.add(b.getUTF());
        }
    }

    @Override
    public void write(ByteBufferWriter b) {
        b.putBoolean(this.hidden);
        b.putByte(this.userNames.size());
        for (String userName : this.userNames) {
            b.putUTF(userName);
        }
    }

    @Override
    public boolean isConsistent(IConnection connection) {
        if (!GameServer.server) {
            return true;
        }
        for (String userName : this.userNames) {
            if (GameServer.UserNameToPlayerMap.containsKey(userName)) continue;
            return false;
        }
        return true;
    }

    @Override
    public void processServer(PacketTypes.PacketType packetType, UdpConnection connection) {
        for (String userName : this.userNames) {
            HiddenAuthors.serverSetAuthorHidden(connection.getUserName(), userName, this.hidden);
        }
    }

    @Override
    public void processClient(UdpConnection connection) {
        for (String userName : this.userNames) {
            HiddenAuthors.clientSetAuthorHidden(userName, this.hidden);
        }
    }
}

