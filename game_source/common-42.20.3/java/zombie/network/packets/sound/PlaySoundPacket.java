/*
 * Decompiled with CFR 0.152.
 */
package zombie.network.packets.sound;

import zombie.GameSounds;
import zombie.audio.BaseSoundEmitter;
import zombie.audio.GameSound;
import zombie.audio.parameters.ParameterBulletHitSurface;
import zombie.audio.parameters.ParameterMeleeHitSurface;
import zombie.characters.BaseCharacterSoundEmitter;
import zombie.characters.Capability;
import zombie.characters.IsoGameCharacter;
import zombie.characters.IsoPlayer;
import zombie.core.network.ByteBufferReader;
import zombie.core.network.ByteBufferWriter;
import zombie.core.raknet.UdpConnection;
import zombie.iso.IsoMovingObject;
import zombie.iso.IsoObject;
import zombie.iso.IsoWorld;
import zombie.network.GameServer;
import zombie.network.IConnection;
import zombie.network.JSONField;
import zombie.network.PacketSetting;
import zombie.network.PacketTypes;
import zombie.network.fields.MovingObject;
import zombie.network.packets.INetworkPacket;

@PacketSetting(ordering=0, priority=1, reliability=2, requiredCapability=Capability.LoginOnServer, handlingType=3)
public class PlaySoundPacket
implements INetworkPacket {
    @JSONField
    String name;
    @JSONField
    MovingObject object = new MovingObject();
    @JSONField
    byte flags;
    @JSONField
    ParameterBulletHitSurface.Material bulletHitSurface;
    @JSONField
    ParameterMeleeHitSurface.Material meleeHitSurface;
    private static final ParameterBulletHitSurface.Material[] BULLET_HIT_SURFACE = ParameterBulletHitSurface.Material.values();
    private static final ParameterMeleeHitSurface.Material[] MELEE_HIT_SURFACE = ParameterMeleeHitSurface.Material.values();
    public static final byte SND_FLAG_NONE = 0;
    public static final byte SND_FLAG_LOOP = 1;
    public static final byte SND_FLAG_VOCAL = 2;
    public static final byte SND_FLAG_BULLET_HIT_SURFACE = 4;
    public static final byte SND_FLAG_MELEE_HIT_SURFACE = 8;

    private boolean isLooped() {
        return (this.flags & 1) != 0;
    }

    private boolean isVocal() {
        return (this.flags & 2) != 0;
    }

    @Override
    public void setData(Object ... values2) {
        Enum material;
        Object object;
        this.name = (String)values2[0];
        this.flags = (Byte)values2[1];
        this.object.set((IsoMovingObject)values2[2]);
        this.bulletHitSurface = null;
        this.meleeHitSurface = null;
        if (values2.length > 3 && (object = values2[3]) instanceof ParameterBulletHitSurface.Material) {
            material = (ParameterBulletHitSurface.Material)((Object)object);
            this.bulletHitSurface = material;
            this.flags = (byte)(this.flags | 4);
        }
        if (values2.length > 3 && (object = values2[3]) instanceof ParameterMeleeHitSurface.Material) {
            this.meleeHitSurface = material = (ParameterMeleeHitSurface.Material)((Object)object);
            this.flags = (byte)(this.flags | 8);
        }
    }

    @Override
    public void processServer(PacketTypes.PacketType packetType, UdpConnection connection) {
        IsoMovingObject object = this.getMovingObject();
        if (!this.isConsistent(connection)) {
            return;
        }
        int radius = 70;
        GameSound gameSound = GameSounds.getSound(this.getName());
        if (gameSound != null) {
            radius = Math.max(radius, (int)gameSound.getMaxDistanceOfClips());
        }
        for (int n = 0; n < GameServer.udpEngine.connections.size(); ++n) {
            IsoPlayer p;
            UdpConnection c = GameServer.udpEngine.connections.get(n);
            if (c.getConnectedGUID() == connection.getConnectedGUID() || !c.isFullyConnected() || (p = GameServer.getAnyPlayerFromConnection(c)) == null || object != null && !c.RelevantTo(object.getX(), object.getY(), radius)) continue;
            ByteBufferWriter b2 = c.startPacket();
            PacketTypes.PacketType.PlaySound.doPacket(b2);
            this.write(b2);
            PacketTypes.PacketType.PlaySound.send(c);
        }
    }

    @Override
    public void processClient(UdpConnection connection) {
        IsoMovingObject movingObject = (IsoMovingObject)this.object.getObject();
        if (movingObject instanceof IsoGameCharacter) {
            IsoGameCharacter isoGameCharacter = (IsoGameCharacter)movingObject;
            BaseCharacterSoundEmitter emitter = isoGameCharacter.getEmitter();
            if (!this.isLooped()) {
                if (this.isVocal() && isoGameCharacter instanceof IsoPlayer) {
                    IsoPlayer player = (IsoPlayer)isoGameCharacter;
                    player.playerVoiceSound(this.name);
                } else {
                    if (this.meleeHitSurface != null && isoGameCharacter instanceof IsoPlayer) {
                        IsoPlayer player = (IsoPlayer)isoGameCharacter;
                        player.setMeleeHitSurface(this.meleeHitSurface);
                    }
                    long instance = emitter.playSoundImpl(this.name, null);
                    if (this.bulletHitSurface != null) {
                        isoGameCharacter.getEmitter().setParameterValueByName(instance, "BulletHitSurface", this.bulletHitSurface.label);
                    }
                }
            }
        } else if (movingObject != null) {
            BaseSoundEmitter emitter = movingObject.emitter;
            if (emitter == null) {
                emitter = IsoWorld.instance.getFreeEmitter(movingObject.getX(), movingObject.getY(), movingObject.getZ());
                IsoWorld.instance.takeOwnershipOfEmitter(emitter);
                movingObject.emitter = emitter;
            }
            if (!this.isLooped()) {
                emitter.playSoundImpl(this.name, (IsoObject)null);
            } else {
                emitter.playSoundLoopedImpl(this.name);
            }
            emitter.tick();
        }
    }

    public String getName() {
        return this.name;
    }

    public IsoMovingObject getMovingObject() {
        return (IsoMovingObject)this.object.getObject();
    }

    @Override
    public void parse(ByteBufferReader b, IConnection connection) {
        this.object.parse(b, connection);
        this.name = b.getUTF();
        this.flags = b.getByte();
        if ((this.flags & 4) != 0) {
            this.bulletHitSurface = b.getEnum(ParameterBulletHitSurface.Material.class);
        }
        if ((this.flags & 8) != 0) {
            this.meleeHitSurface = b.getEnum(ParameterMeleeHitSurface.Material.class);
        }
    }

    @Override
    public void write(ByteBufferWriter b) {
        this.object.write(b);
        b.putUTF(this.name);
        b.putByte(this.flags);
        if (this.bulletHitSurface != null) {
            b.putEnum(this.bulletHitSurface);
        } else if (this.meleeHitSurface != null) {
            b.putEnum(this.meleeHitSurface);
        }
    }

    @Override
    public boolean isConsistent(IConnection connection) {
        return this.name != null && !this.name.isEmpty();
    }

    @Override
    public int getPacketSizeBytes() {
        int enumBytes = (this.flags & 0xC) != 0 ? 1 : 0;
        return this.object.getPacketSizeBytes() + this.name.length() + 1 + enumBytes;
    }
}

