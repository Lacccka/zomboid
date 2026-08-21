/*
 * Decompiled with CFR 0.152.
 */
package zombie.network.anticheats;

import zombie.characters.Faction;
import zombie.characters.IsoGameCharacter;
import zombie.characters.IsoPlayer;
import zombie.characters.SafetySystemManager;
import zombie.core.raknet.UdpConnection;
import zombie.debug.DebugType;
import zombie.iso.areas.NonPvpZone;
import zombie.network.ServerOptions;
import zombie.network.anticheats.AbstractAntiCheat;
import zombie.network.packets.INetworkPacket;
import zombie.util.Type;

public class AntiCheatSafety
extends AbstractAntiCheat {
    @Override
    public String validate(UdpConnection connection, INetworkPacket packet) {
        String result = super.validate(connection, packet);
        if (!(packet instanceof IAntiCheat)) {
            DebugType.Multiplayer.error("Invalid packet-type=%s for anti-cheat=%s", packet.getClass().getSimpleName(), this.getClass().getSimpleName());
            return "";
        }
        IAntiCheat field = (IAntiCheat)((Object)packet);
        IsoPlayer wielder = Type.tryCastTo(field.getWielder(), IsoPlayer.class);
        if (wielder == null) {
            return "wielder not found";
        }
        IsoPlayer target = Type.tryCastTo(field.getTarget(), IsoPlayer.class);
        if (target == null) {
            return "target not found";
        }
        if (target.isGodMod()) {
            return "target is in god-mode";
        }
        if (!ServerOptions.instance.pvp.getValue()) {
            return "server pvp is disabled";
        }
        boolean isInSameFaction = Faction.isInSameFaction(wielder, target);
        if (isInSameFaction && !field.isMelee()) {
            return "only melee pvp is allowed for faction members";
        }
        if (isInSameFaction && AntiCheatSafety.isFactionPvPBlocked(wielder, target)) {
            return "faction melee pvp is disabled by players";
        }
        if (ServerOptions.instance.safetySystem.getValue() && AntiCheatSafety.isPvPBlocked(wielder, target)) {
            return "pvp is disabled by players";
        }
        boolean isWielderInNonPvpZone = NonPvpZone.isInNonPvpZone(wielder);
        if (isWielderInNonPvpZone && AntiCheatSafety.isInNonPvPBlockedState(wielder)) {
            return "wielder is in non-pvp zone";
        }
        if (isWielderInNonPvpZone) {
            return "";
        }
        boolean isTargetInNonPvpZone = NonPvpZone.isInNonPvpZone(target);
        if (isTargetInNonPvpZone && AntiCheatSafety.isInNonPvPBlockedState(target)) {
            return "target is in non-pvp zone";
        }
        if (isTargetInNonPvpZone) {
            return "";
        }
        return result;
    }

    private static boolean isPvPBlocked(IsoPlayer wielder, IsoPlayer target) {
        return wielder.getSafety().isEnabled() && target.getSafety().isEnabled();
    }

    private static boolean isFactionPvPBlocked(IsoPlayer wielder, IsoPlayer target) {
        return !wielder.isFactionPvp() && !target.isFactionPvp();
    }

    private static boolean isInNonPvPBlockedState(IsoPlayer player) {
        return SafetySystemManager.isPlayerSafetyChangeDelayed(player);
    }

    public static interface IAntiCheat {
        public IsoGameCharacter getTarget();

        public IsoPlayer getWielder();

        default public boolean isMelee() {
            return false;
        }
    }
}

