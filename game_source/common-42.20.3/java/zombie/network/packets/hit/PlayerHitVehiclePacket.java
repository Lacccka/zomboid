/*
 * Decompiled with CFR 0.152.
 */
package zombie.network.packets.hit;

import java.util.List;
import zombie.CombatManager;
import zombie.characters.Capability;
import zombie.characters.IsoPlayer;
import zombie.core.network.ByteBufferReader;
import zombie.core.network.ByteBufferWriter;
import zombie.inventory.types.HandWeapon;
import zombie.iso.IsoUtils;
import zombie.network.GameServer;
import zombie.network.IConnection;
import zombie.network.JSONField;
import zombie.network.PacketSetting;
import zombie.network.anticheats.AntiCheat;
import zombie.network.anticheats.AntiCheatHitLongDistance;
import zombie.network.fields.hit.TracerInfo;
import zombie.network.fields.vehicle.VehicleID;
import zombie.network.packets.hit.PlayerHit;
import zombie.vehicles.BaseVehicle;

@PacketSetting(ordering=0, priority=0, reliability=3, requiredCapability=Capability.LoginOnServer, handlingType=3, anticheats={AntiCheat.HitLongDistance})
public class PlayerHitVehiclePacket
extends PlayerHit
implements AntiCheatHitLongDistance.IAntiCheat {
    @JSONField
    protected final VehicleID vehicleId = new VehicleID();
    @JSONField
    protected float damage;

    @Override
    public void setData(Object ... values2) {
        this.set((IsoPlayer)values2[0], (HandWeapon)values2[1], (Boolean)values2[2], (Boolean)values2[3], (List)values2[4], (BaseVehicle)values2[5], ((Float)values2[6]).floatValue());
    }

    public void set(IsoPlayer wielder, HandWeapon weapon, boolean isIgnoreDamage, boolean isCriticalHit, List<TracerInfo> tracers, BaseVehicle vehicle, float damage) {
        this.set(wielder, weapon, isIgnoreDamage, isCriticalHit, tracers);
        this.vehicleId.set(vehicle);
        this.damage = damage;
    }

    @Override
    public void parse(ByteBufferReader b, IConnection connection) {
        super.parse(b, connection);
        this.vehicleId.parse(b, connection);
        this.damage = b.getFloat();
    }

    @Override
    public void write(ByteBufferWriter b) {
        super.write(b);
        this.vehicleId.write(b);
        b.putFloat(this.damage);
    }

    @Override
    public boolean isConsistent(IConnection connection) {
        return super.isConsistent(connection) && this.vehicleId.isConsistent(connection);
    }

    @Override
    public void process() {
        if (GameServer.server) {
            this.vehicleId.getVehicle().processHit(this.wielder.getCharacter(), this.getHandWeapon(), this.damage);
            if (this.getHandWeapon().getPhysicsObject() == null) {
                CombatManager.getInstance().processMaintenanceCheck(this.wielder.getCharacter(), this.getHandWeapon(), this.vehicleId.getVehicle());
            }
        }
    }

    @Override
    public float getDistance() {
        return IsoUtils.DistanceTo(this.vehicleId.getX(), this.vehicleId.getY(), this.wielder.getX(), this.wielder.getY());
    }
}

