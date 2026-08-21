/*
 * Decompiled with CFR 0.152.
 */
package zombie.network.packets.hit;

import java.util.List;
import zombie.CombatManager;
import zombie.characters.BodyDamage.BodyPartType;
import zombie.characters.Capability;
import zombie.characters.IsoGameCharacter;
import zombie.characters.IsoPlayer;
import zombie.core.logger.LoggerManager;
import zombie.core.network.ByteBufferReader;
import zombie.core.network.ByteBufferWriter;
import zombie.core.raknet.UdpConnection;
import zombie.core.random.Rand;
import zombie.inventory.types.HandWeapon;
import zombie.iso.IsoUtils;
import zombie.network.GameServer;
import zombie.network.IConnection;
import zombie.network.JSONField;
import zombie.network.PVPLogTool;
import zombie.network.PacketSetting;
import zombie.network.anticheats.AntiCheat;
import zombie.network.anticheats.AntiCheatHitDamage;
import zombie.network.anticheats.AntiCheatHitLongDistance;
import zombie.network.anticheats.AntiCheatHitWeapon;
import zombie.network.anticheats.AntiCheatSafety;
import zombie.network.fields.hit.Fall;
import zombie.network.fields.hit.Hit;
import zombie.network.fields.hit.Player;
import zombie.network.fields.hit.TracerInfo;
import zombie.network.fields.hit.WeaponHit;
import zombie.network.packets.hit.PlayerHit;

@PacketSetting(ordering=0, priority=0, reliability=3, requiredCapability=Capability.LoginOnServer, handlingType=3, anticheats={AntiCheat.HitDamage, AntiCheat.HitLongDistance, AntiCheat.HitWeapon, AntiCheat.Safety})
public class PlayerHitPlayerPacket
extends PlayerHit
implements AntiCheatHitDamage.IAntiCheat,
AntiCheatHitLongDistance.IAntiCheat,
AntiCheatHitWeapon.IAntiCheat,
AntiCheatSafety.IAntiCheat {
    @JSONField
    public final Player target = new Player();
    @JSONField
    protected final WeaponHit hit = new WeaponHit();
    @JSONField
    protected final Fall fall = new Fall();

    @Override
    public void setData(Object ... values2) {
        this.set((IsoPlayer)values2[0], (HandWeapon)values2[1], (Boolean)values2[2], (Boolean)values2[3], (List)values2[4], (IsoPlayer)values2[5], ((Float)values2[6]).floatValue(), ((Float)values2[7]).floatValue(), (Boolean)values2[8], (Boolean)values2[9]);
    }

    public void set(IsoPlayer wielder, HandWeapon weapon, boolean isIgnoreDamage, boolean isCriticalHit, List<TracerInfo> tracers, IsoPlayer target, float damage, float range, boolean hitHead, boolean hitLegs) {
        this.set(wielder, weapon, isIgnoreDamage, isCriticalHit, tracers);
        this.target.set(target, false);
        this.hit.set(damage, range, target.getHitForce(), target.getHitDir().x, target.getHitDir().y, hitHead, hitLegs, false);
        this.fall.set(target.getHitReactionNetworkAI());
    }

    @Override
    public void parse(ByteBufferReader b, IConnection connection) {
        super.parse(b, connection);
        this.target.parse(b, connection);
        this.hit.parse(b, connection);
        this.fall.parse(b, connection);
    }

    @Override
    public void write(ByteBufferWriter b) {
        super.write(b);
        this.target.write(b);
        this.hit.write(b);
        this.fall.write(b);
    }

    @Override
    public boolean isConsistent(IConnection connection) {
        return super.isConsistent(connection) && this.target.isConsistent(connection) && this.hit.isConsistent(connection);
    }

    @Override
    public void update() {
        super.update();
        this.fall.set(this.target.getCharacter().realx, this.target.getCharacter().realy, this.target.getCharacter().realz, this.fall.dropDirection);
    }

    @Override
    public void preProcess() {
        super.preProcess();
        this.target.process();
    }

    @Override
    public void process() {
        this.hit.process(this.wielder.getCharacter(), this.target.getCharacter(), this.getHandWeapon(), this.isIgnoreDamage());
        this.fall.process(this.target.getCharacter());
        if (GameServer.server) {
            CombatManager.getInstance().applyMeleeEnduranceLoss(this.wielder.getCharacter(), this.target.getCharacter(), this.getHandWeapon(), this.hit.getDamage());
            if (this.getHandWeapon().isMelee()) {
                this.resolveSpikedArmorDamage();
            }
        }
    }

    @Override
    public void postProcess() {
        super.postProcess();
        this.target.process();
    }

    @Override
    public void log(UdpConnection connection) {
        PVPLogTool.logCombat(this.wielder.getPlayer().getUsername(), LoggerManager.getPlayerCoords(this.wielder.getPlayer()), this.target.getPlayer().getUsername(), LoggerManager.getPlayerCoords(this.target.getPlayer()), this.wielder.getPlayer().getX(), this.wielder.getPlayer().getY(), this.wielder.getPlayer().getZ(), this.getHandWeapon().getName(), this.hit.getDamage());
    }

    @Override
    public void attack() {
        this.wielder.attack(this.getHandWeapon(), true, this.shotID);
        this.processTracers();
    }

    @Override
    public void react() {
        this.target.react();
    }

    @Override
    public float getDistance() {
        return IsoUtils.DistanceTo(this.target.getX(), this.target.getY(), this.wielder.getX(), this.wielder.getY());
    }

    @Override
    public Hit getHit() {
        return this.hit;
    }

    @Override
    public IsoGameCharacter getTarget() {
        return this.target.getPlayer();
    }

    @Override
    public IsoPlayer getWielder() {
        return this.wielder.getPlayer();
    }

    @Override
    public boolean isMelee() {
        return this.getHandWeapon().isMelee();
    }

    private void resolveSpikedArmorDamage() {
        int partHit = this.hit.isHitHead() ? Rand.Next(BodyPartType.ToIndex(BodyPartType.Head), BodyPartType.ToIndex(BodyPartType.Neck) + 1) : (this.hit.isHitLegs() ? Rand.Next(BodyPartType.ToIndex(BodyPartType.Groin), BodyPartType.ToIndex(BodyPartType.Foot_R) + 1) : Rand.Next(BodyPartType.ToIndex(BodyPartType.Hand_L), BodyPartType.ToIndex(BodyPartType.Neck) + 1));
        CombatManager.getInstance().resolveSpikedArmorDamage(this.wielder.getCharacter(), this.getHandWeapon(), this.target.getCharacter(), partHit);
    }
}

