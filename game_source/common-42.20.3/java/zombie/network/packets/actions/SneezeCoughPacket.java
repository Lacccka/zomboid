/*
 * Decompiled with CFR 0.152.
 */
package zombie.network.packets.actions;

import zombie.characters.Capability;
import zombie.characters.IsoPlayer;
import zombie.core.Translator;
import zombie.core.network.ByteBufferReader;
import zombie.core.network.ByteBufferWriter;
import zombie.core.raknet.UdpConnection;
import zombie.network.IConnection;
import zombie.network.PacketSetting;
import zombie.network.fields.character.PlayerID;
import zombie.network.packets.INetworkPacket;

@PacketSetting(ordering=0, priority=3, reliability=0, requiredCapability=Capability.LoginOnServer, handlingType=2)
public class SneezeCoughPacket
implements INetworkPacket {
    protected final PlayerID wielder = new PlayerID();
    protected byte value;

    @Override
    public void setData(Object ... values2) {
        this.set((IsoPlayer)values2[0], (Integer)values2[1], (Byte)values2[2]);
    }

    public void set(IsoPlayer wielder, int sneezingCoughing, byte sneezeVar) {
        this.wielder.set(wielder);
        this.value = 0;
        if (sneezingCoughing % 2 == 0) {
            this.value = (byte)(this.value | 1);
        }
        if (sneezingCoughing > 2) {
            this.value = (byte)(this.value | 2);
        }
        if (sneezeVar > 1) {
            this.value = (byte)(this.value | 4);
        }
    }

    @Override
    public void write(ByteBufferWriter b) {
        this.wielder.write(b);
        b.putByte(this.value);
    }

    @Override
    public void parse(ByteBufferReader b, IConnection connection) {
        this.wielder.parse(b, connection);
        this.value = b.getByte();
    }

    @Override
    public void processClient(UdpConnection connection) {
        if (this.wielder.getPlayer() != null) {
            int sneezeVar;
            boolean isSneeze = (this.value & 1) == 0;
            boolean isMuffled = (this.value & 2) != 0;
            int n = sneezeVar = (this.value & 4) == 0 ? 1 : 2;
            if (this.wielder.getPlayer().isLocalPlayer()) {
                this.wielder.getPlayer().setVariable("Ext", (String)(isSneeze ? "Sneeze" + sneezeVar : "Cough"));
                this.wielder.getPlayer().reportEvent("EventDoExt");
            }
            this.wielder.getPlayer().Say(Translator.getText("IGUI_PlayerText_" + (isSneeze ? "Sneeze" : "Cough") + (isMuffled ? "Muffled" : ""), new Object[0]));
            if (isSneeze) {
                if (isMuffled) {
                    this.wielder.getPlayer().playerVoiceSound("SneezeLight");
                } else {
                    this.wielder.getPlayer().playerVoiceSound("SneezeHeavy");
                }
            } else if (isMuffled) {
                this.wielder.getPlayer().playerVoiceSound("MuffledCough");
            } else {
                this.wielder.getPlayer().playerVoiceSound("Cough");
            }
        }
    }

    @Override
    public boolean isConsistent(IConnection connection) {
        return this.wielder.isConsistent(connection);
    }
}

