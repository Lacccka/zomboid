/*
 * Decompiled with CFR 0.152.
 */
package zombie.network.packets.sound;

import fmod.fmod.FMODManager;
import zombie.GameSounds;
import zombie.audio.BaseSoundEmitter;
import zombie.audio.FMODParameterUtils;
import zombie.audio.GameSound;
import zombie.characters.Capability;
import zombie.characters.IsoPlayer;
import zombie.core.network.ByteBufferReader;
import zombie.core.network.ByteBufferWriter;
import zombie.core.raknet.UdpConnection;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoObject;
import zombie.iso.IsoWorld;
import zombie.network.GameClient;
import zombie.network.GameServer;
import zombie.network.IConnection;
import zombie.network.JSONField;
import zombie.network.PacketSetting;
import zombie.network.PacketTypes;
import zombie.network.packets.INetworkPacket;
import zombie.util.StringUtils;

@PacketSetting(ordering=0, priority=1, reliability=2, requiredCapability=Capability.LoginOnServer, handlingType=3)
public class RangedWeaponSoundPacket
implements INetworkPacket {
    @JSONField
    String soundName;
    @JSONField
    float x;
    @JSONField
    float y;
    @JSONField
    byte z;
    @JSONField
    short playerId;
    @JSONField
    float firearmInside;
    @JSONField
    float firearmRoomSize;

    @Override
    public void setData(Object ... values2) {
        this.soundName = (String)values2[0];
        this.x = ((Float)values2[1]).floatValue();
        this.y = ((Float)values2[2]).floatValue();
        this.z = ((Float)values2[3]).byteValue();
        this.playerId = (Short)values2[4];
        this.firearmInside = ((Float)values2[5]).floatValue();
        this.firearmRoomSize = ((Float)values2[6]).floatValue();
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
            PacketTypes.PacketType.RangedWeaponSound.doPacket(b2);
            this.write(b2);
            PacketTypes.PacketType.RangedWeaponSound.send(c);
        }
    }

    @Override
    public void processClient(UdpConnection connection) {
        BaseSoundEmitter emitter = IsoWorld.instance.getFreeEmitter();
        emitter.setPlayRemoteEvents(true);
        emitter.setPos(this.x, this.y, this.z);
        long instance = emitter.playSoundImpl(this.soundName, (IsoObject)null);
        emitter.setParameterValueByName(instance, "FirearmInside", this.firearmInside);
        emitter.setParameterValueByName(instance, "FirearmRoomSize", this.firearmRoomSize);
        emitter.setParameterValueByName(instance, "LocalPlayer", 0.0f);
        IsoGridSquare square = IsoWorld.instance.currentCell.getGridSquare(this.x, this.y, (double)this.z);
        IsoPlayer listener = FMODParameterUtils.getClosestListener(this.x, this.y, this.z);
        float occlusion = listener == null || square == null ? 1.0f : (square.isCouldSee(listener.getIndex()) ? 0.0f : 1.0f);
        emitter.setParameterValue(instance, FMODManager.instance.getParameterDescription("Occlusion"), occlusion);
        emitter.tick();
    }

    @Override
    public void parse(ByteBufferReader b, IConnection connection) {
        this.soundName = b.getUTF();
        this.x = b.getFloat();
        this.y = b.getFloat();
        this.z = b.getByte();
        this.playerId = b.getShort();
        this.firearmInside = b.getFloat();
        this.firearmRoomSize = b.getFloat();
    }

    @Override
    public void write(ByteBufferWriter b) {
        b.putUTF(this.soundName);
        b.putFloat(this.x);
        b.putFloat(this.y);
        b.putByte(this.z);
        b.putShort(this.playerId);
        b.putFloat(this.firearmInside);
        b.putFloat(this.firearmRoomSize);
    }

    @Override
    public boolean isConsistent(IConnection connection) {
        if (GameClient.client) {
            return !StringUtils.isNullOrWhitespace(this.soundName);
        }
        return !StringUtils.isNullOrWhitespace(this.soundName) && connection.hasPlayer(this.playerId);
    }
}

