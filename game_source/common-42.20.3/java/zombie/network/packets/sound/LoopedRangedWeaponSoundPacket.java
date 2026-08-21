/*
 * Decompiled with CFR 0.152.
 */
package zombie.network.packets.sound;

import zombie.GameSounds;
import zombie.audio.GameSound;
import zombie.audio.LoopedRangedWeaponSounds;
import zombie.characters.Capability;
import zombie.characters.IsoPlayer;
import zombie.core.network.ByteBufferReader;
import zombie.core.network.ByteBufferWriter;
import zombie.core.raknet.UdpConnection;
import zombie.inventory.InventoryItem;
import zombie.inventory.types.HandWeapon;
import zombie.network.GameClient;
import zombie.network.GameServer;
import zombie.network.IConnection;
import zombie.network.JSONField;
import zombie.network.PacketSetting;
import zombie.network.PacketTypes;
import zombie.network.packets.INetworkPacket;
import zombie.util.StringUtils;

@PacketSetting(ordering=0, priority=1, reliability=2, requiredCapability=Capability.LoginOnServer, handlingType=3)
public class LoopedRangedWeaponSoundPacket
implements INetworkPacket {
    @JSONField
    boolean stop;
    @JSONField
    short playerId;
    @JSONField
    boolean cancelPrevious;
    @JSONField
    String soundName;
    @JSONField
    float x;
    @JSONField
    float y;
    @JSONField
    byte z;
    @JSONField
    float firearmInside;
    @JSONField
    float firearmRoomSize;

    @Override
    public void setData(Object ... values2) {
        this.stop = (Boolean)values2[0];
        this.playerId = (Short)values2[1];
        if (this.stop) {
            this.cancelPrevious = (Boolean)values2[2];
        } else {
            this.soundName = (String)values2[2];
            this.x = ((Float)values2[3]).floatValue();
            this.y = ((Float)values2[4]).floatValue();
            this.z = ((Float)values2[5]).byteValue();
            this.firearmInside = ((Float)values2[6]).floatValue();
            this.firearmRoomSize = ((Float)values2[7]).floatValue();
        }
    }

    @Override
    public void processServer(PacketTypes.PacketType packetType, UdpConnection connection) {
        if (!this.isConsistent(connection)) {
            return;
        }
        int radius = 70;
        GameSound gameSound = GameSounds.getSound(this.soundName);
        if (gameSound != null) {
            radius = Math.max(radius, (int)gameSound.getMaxDistanceOfClips());
        }
        for (int n = 0; n < GameServer.udpEngine.connections.size(); ++n) {
            IsoPlayer p;
            UdpConnection c = GameServer.udpEngine.connections.get(n);
            if (c.getConnectedGUID() == connection.getConnectedGUID() || !c.isFullyConnected() || (p = GameServer.getAnyPlayerFromConnection(c)) == null || !c.RelevantTo(this.x, this.y, radius)) continue;
            ByteBufferWriter b2 = c.startPacket();
            PacketTypes.PacketType.LoopedRangedWeaponSound.doPacket(b2);
            this.write(b2);
            PacketTypes.PacketType.LoopedRangedWeaponSound.send(c);
        }
    }

    @Override
    public void processClient(UdpConnection connection) {
        Object owner = LoopedRangedWeaponSounds.INSTANCE.getOwnerObjectForRemotePlayer(this.playerId);
        if (this.stop) {
            LoopedRangedWeaponSounds.INSTANCE.stopAttackLoopSound(owner, this.cancelPrevious);
        } else {
            LoopedRangedWeaponSounds.INSTANCE.setPosition(owner, this.x, this.y, this.z);
            LoopedRangedWeaponSounds.INSTANCE.setParameters(owner, this.firearmInside, this.firearmRoomSize);
            LoopedRangedWeaponSounds.INSTANCE.startAttackLoopSound(owner, this.soundName);
        }
    }

    @Override
    public void parse(ByteBufferReader b, IConnection connection) {
        this.stop = b.getBoolean();
        this.playerId = b.getShort();
        if (this.stop) {
            this.cancelPrevious = b.getBoolean();
        } else {
            this.soundName = b.getUTF();
            this.x = b.getFloat();
            this.y = b.getFloat();
            this.z = b.getByte();
            this.firearmInside = b.getFloat();
            this.firearmRoomSize = b.getFloat();
        }
    }

    @Override
    public void write(ByteBufferWriter b) {
        b.putBoolean(this.stop);
        b.putShort(this.playerId);
        if (this.stop) {
            b.putBoolean(this.cancelPrevious);
        } else {
            b.putUTF(this.soundName);
            b.putFloat(this.x);
            b.putFloat(this.y);
            b.putByte(this.z);
            b.putFloat(this.firearmInside);
            b.putFloat(this.firearmRoomSize);
        }
    }

    @Override
    public boolean isConsistent(IConnection connection) {
        if (GameClient.client) {
            return !StringUtils.isNullOrWhitespace(this.soundName);
        }
        if (!connection.hasPlayer(this.playerId)) {
            return false;
        }
        IsoPlayer player = GameServer.IDToPlayerMap.get(this.playerId);
        if (player == null) {
            return false;
        }
        if (!this.stop) {
            InventoryItem inventoryItem = player.getPrimaryHandItem();
            if (inventoryItem instanceof HandWeapon) {
                int ammoCount;
                HandWeapon weapon = (HandWeapon)inventoryItem;
                int n = ammoCount = player.isUnlimitedAmmo() ? weapon.getMaxAmmo() : weapon.getCurrentAmmoCount();
                if (!weapon.isAimedFirearm() || ammoCount == 0) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return this.stop || !StringUtils.isNullOrWhitespace(this.soundName);
    }
}

