/*
 * Decompiled with CFR 0.152.
 */
package zombie.characters;

import zombie.characters.IsoZombie;
import zombie.util.flags.OrdinalShortFlag;
import zombie.util.flags.ShortFlags;

public class NetworkZombieVariables {
    public static ShortFlags getBooleanVariables(IsoZombie zombie) {
        ShortFlags flags = ShortFlags.alloc();
        flags.set(Flag.IsFakeDead, zombie.isFakeDead());
        flags.set(Flag.IsLunger, zombie.lunger);
        flags.set(Flag.IsRunning, zombie.running);
        flags.set(Flag.IsCrawling, zombie.isCrawling());
        flags.set(Flag.IsSitAgainstWall, zombie.isSitAgainstWall());
        flags.set(Flag.IsReanimatedPlayer, zombie.isReanimatedPlayer());
        flags.set(Flag.IsOnFire, zombie.isOnFire());
        flags.set(Flag.IsUseless, zombie.isUseless());
        flags.set(Flag.IsOnFloor, zombie.isOnFloor());
        flags.set(Flag.IsReanimatedForGrappleOnly, zombie.isReanimatedForGrappleOnly());
        flags.set(Flag.IsCanWalk, zombie.isCanWalk());
        flags.set(Flag.IsSkeleton, zombie.isSkeleton());
        flags.set(Flag.IsFallOnFront, zombie.isFallOnFront());
        flags.set(Flag.IsAnimationRecording, zombie.isAnimationRecorderActive());
        return flags;
    }

    public static void setBooleanVariables(IsoZombie zombie, short val) {
        NetworkZombieVariables.setBooleanVariables(zombie, ShortFlags.toFlags(val));
    }

    public static void setBooleanVariables(IsoZombie zombie, ShortFlags val) {
        zombie.setFakeDead(val.has(Flag.IsFakeDead));
        zombie.lunger = val.has(Flag.IsLunger);
        zombie.running = val.has(Flag.IsRunning);
        zombie.setCrawler(val.has(Flag.IsCrawling));
        zombie.setSitAgainstWall(val.has(Flag.IsSitAgainstWall));
        zombie.setReanimatedPlayer(val.has(Flag.IsReanimatedPlayer));
        if (val.has(Flag.IsOnFire)) {
            zombie.SetOnFire();
        } else {
            zombie.StopBurning();
        }
        zombie.setUseless(val.has(Flag.IsUseless));
        if (zombie.isReanimatedPlayer()) {
            zombie.setOnFloor(val.has(Flag.IsOnFloor));
        }
        zombie.setReanimatedForGrappleOnly(val.has(Flag.IsReanimatedForGrappleOnly));
        zombie.setCanWalk(val.has(Flag.IsCanWalk));
        zombie.setSkeleton(val.has(Flag.IsSkeleton));
        zombie.setFallOnFront(val.has(Flag.IsFallOnFront));
        zombie.setAnimRecorderActive(val.has(Flag.IsAnimationRecording), true);
    }

    public static enum Flag implements OrdinalShortFlag
    {
        IsFakeDead,
        IsLunger,
        IsRunning,
        IsCrawling,
        IsSitAgainstWall,
        IsReanimatedPlayer,
        IsOnFire,
        IsUseless,
        IsOnFloor,
        IsReanimatedForGrappleOnly,
        IsCanWalk,
        IsSkeleton,
        IsFallOnFront,
        IsAnimationRecording;

    }
}

