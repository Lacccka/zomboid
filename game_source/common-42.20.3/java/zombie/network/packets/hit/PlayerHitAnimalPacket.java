/*
 * Decompiled with CFR 0.152.
 */
package zombie.network.packets.hit;

import java.util.List;
import zombie.characters.Capability;
import zombie.characters.IsoPlayer;
import zombie.characters.animals.IsoAnimal;
import zombie.core.network.ByteBufferReader;
import zombie.core.network.ByteBufferWriter;
import zombie.inventory.types.HandWeapon;
import zombie.iso.IsoUtils;
import zombie.network.IConnection;
import zombie.network.JSONField;
import zombie.network.PacketSetting;
import zombie.network.anticheats.AntiCheat;
import zombie.network.anticheats.AntiCheatHitDamage;
import zombie.network.anticheats.AntiCheatHitLongDistance;
import zombie.network.anticheats.AntiCheatHitWeapon;
import zombie.network.fields.character.AnimalID;
import zombie.network.fields.hit.Hit;
import zombie.network.fields.hit.TracerInfo;
import zombie.network.fields.hit.WeaponHit;
import zombie.network.packets.hit.PlayerHit;

@PacketSetting(ordering=0, priority=0, reliability=3, requiredCapability=Capability.LoginOnServer, handlingType=3, anticheats={AntiCheat.HitDamage, AntiCheat.HitLongDistance, AntiCheat.HitWeapon})
public class PlayerHitAnimalPacket
extends PlayerHit
implements AntiCheatHitDamage.IAntiCheat,
AntiCheatHitLongDistance.IAntiCheat,
AntiCheatHitWeapon.IAntiCheat {
    @JSONField
    protected final AnimalID target = new AnimalID();
    @JSONField
    protected final WeaponHit hit = new WeaponHit();

    @Override
    public void setData(Object ... values2) {
        this.set((IsoPlayer)values2[0], (HandWeapon)values2[1], (Boolean)values2[2], (Boolean)values2[3], (List)values2[4], (IsoAnimal)values2[5], ((Float)values2[6]).floatValue(), ((Float)values2[7]).floatValue(), (Boolean)values2[8]);
    }

    public void set(IsoPlayer wielder, HandWeapon weapon, boolean isIgnoreDamage, boolean isCriticalHit, List<TracerInfo> tracers, IsoAnimal target, float damage, float range, boolean hitHead) {
        this.set(wielder, weapon, isIgnoreDamage, isCriticalHit, tracers);
        this.target.set(target);
        this.hit.set(damage, range, target.getHitForce(), target.getHitDir().x, target.getHitDir().y, hitHead, false, false);
    }

    @Override
    public void parse(ByteBufferReader b, IConnection connection) {
        super.parse(b, connection);
        this.target.parse(b, connection);
        this.hit.parse(b, connection);
    }

    @Override
    public void write(ByteBufferWriter b) {
        super.write(b);
        this.target.write(b);
        this.hit.write(b);
    }

    @Override
    public boolean isConsistent(IConnection connection) {
        return super.isConsistent(connection) && this.target.isConsistent(connection) && this.hit.isConsistent(connection);
    }

    @Override
    public void process() {
        this.hit.process(this.wielder.getCharacter(), this.target.getAnimal(), this.getHandWeapon(), this.isIgnoreDamage());
    }

    @Override
    public void attack() {
        this.wielder.attack(this.getHandWeapon(), false, this.shotID);
        this.processTracers();
    }

    @Override
    public float getDistance() {
        return IsoUtils.DistanceTo(this.target.getX(), this.target.getY(), this.wielder.getX(), this.wielder.getY());
    }

    @Override
    public IsoPlayer getWielder() {
        return this.wielder.getPlayer();
    }

    @Override
    public Hit getHit() {
        return this.hit;
    }
}

