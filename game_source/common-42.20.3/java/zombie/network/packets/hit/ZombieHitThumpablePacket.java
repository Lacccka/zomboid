/*
 * Decompiled with CFR 0.152.
 */
package zombie.network.packets.hit;

import gnu.trove.iterator.TShortObjectIterator;
import gnu.trove.map.hash.TShortObjectHashMap;
import zombie.characters.Capability;
import zombie.characters.IsoZombie;
import zombie.core.network.ByteBufferReader;
import zombie.core.network.ByteBufferWriter;
import zombie.core.raknet.UdpConnection;
import zombie.iso.IsoObject;
import zombie.network.GameServer;
import zombie.network.IConnection;
import zombie.network.PacketSetting;
import zombie.network.PacketTypes;
import zombie.network.anticheats.AntiCheat;
import zombie.network.anticheats.AntiCheatHitShortDistance;
import zombie.network.fields.character.ZombieID;
import zombie.network.fields.hit.Thumpable;
import zombie.network.packets.hit.HitCharacter;

@PacketSetting(ordering=0, priority=0, reliability=3, requiredCapability=Capability.LoginOnServer, handlingType=3, anticheats={AntiCheat.HitShortDistance})
public class ZombieHitThumpablePacket
implements HitCharacter,
AntiCheatHitShortDistance.IAntiCheat {
    private final TShortObjectHashMap<IsoObject> thumpHitsToReceive = new TShortObjectHashMap();
    private TShortObjectHashMap<IsoObject> thumpHitsToSend;
    private final ZombieID zombieID = new ZombieID();
    private final Thumpable thumpable = new Thumpable();
    private float furthestDistance;

    @Override
    public void setData(Object ... values2) {
        this.thumpHitsToSend = (TShortObjectHashMap)values2[0];
    }

    @Override
    public boolean isRelevant(UdpConnection connection) {
        TShortObjectIterator<IsoObject> iterator2 = this.thumpHitsToReceive.iterator();
        while (iterator2.hasNext()) {
            iterator2.advance();
            if (!connection.isRelevantTo(iterator2.value().getX(), iterator2.value().getY())) continue;
            return true;
        }
        return false;
    }

    @Override
    public void write(ByteBufferWriter b) {
        b.putShort(this.thumpHitsToSend.size());
        TShortObjectIterator<IsoObject> iterator2 = this.thumpHitsToSend.iterator();
        while (iterator2.hasNext()) {
            iterator2.advance();
            b.putShort(iterator2.key());
            this.thumpable.set(iterator2.value());
            this.thumpable.write(b);
        }
    }

    @Override
    public void parse(ByteBufferReader b, IConnection connection) {
        this.thumpHitsToReceive.clear();
        float maxDistSq = 0.0f;
        int size = b.getShort();
        for (int i = 0; i < size; ++i) {
            this.zombieID.parse(b, connection);
            if (!this.zombieID.isConsistent(connection)) continue;
            IsoZombie zombie = this.zombieID.getZombie();
            if (GameServer.server && zombie.getOwner() != connection) continue;
            this.thumpable.parse(b, connection);
            if (!this.thumpable.isConsistent(connection)) continue;
            this.thumpable.process(zombie);
            this.thumpHitsToReceive.put(this.zombieID.getID(), this.thumpable.getIsoObject());
            float dx = this.thumpable.getX() - zombie.getX();
            float dy = this.thumpable.getY() - zombie.getY();
            float distSq = dx * dx + dy * dy;
            if (!(distSq > maxDistSq)) continue;
            maxDistSq = distSq;
        }
        this.furthestDistance = (float)Math.sqrt(maxDistSq);
        if (GameServer.server) {
            this.thumpHitsToSend = this.thumpHitsToReceive;
            this.sendToClient(PacketTypes.PacketType.ZombieHitThumpable, connection);
        }
    }

    @Override
    public float getFurthestHitDistance() {
        return this.furthestDistance;
    }
}

