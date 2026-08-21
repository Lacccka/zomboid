/*
 * Decompiled with CFR 0.152.
 */
package zombie.input;

import zombie.GameTime;
import zombie.characters.IsoPlayer;
import zombie.core.math.PZMath;
import zombie.core.math.interpolators.LerpType;
import zombie.iso.IsoCamera;
import zombie.iso.PlayerCamera;
import zombie.iso.Vector2;

public enum AimingMode {
    UNARMED(true, -1.0f, -1.0f, 0.05f, false),
    UNARMED_TARGET_FOUND(true, -1.0f, -1.0f, 0.05f, true),
    MELEE(true, -1.0f, -1.0f, 0.05f, false),
    MELEE_TARGET_FOUND(true, -1.0f, -1.0f, 0.05f, true),
    RANGED(true, -1.0f, -1.0f, 0.05f, false),
    RANGED_TARGET_FOUND(true, -1.0f, -1.0f, 0.05f, true),
    RANGED_PRECISE(false, 0.065f, 0.095f, 0.0f, false),
    RANGED_PRECISE_TARGET_FOUND(false, 0.03f, 0.085f, 0.0f, true),
    NOT_AIMING(true, -1.0f, -1.0f, -1.0f, false);

    public final float lerpRateOutwards;
    public final float lerpRateInwards;
    public final float minDistanceAmount;
    public final boolean fixedAtMinDistance;
    public final boolean hasTarget;

    private AimingMode(boolean fixedAtMinDistance, float lerpRateOutwards, float lerpRateInwards, float minDistanceAmount, boolean hasTarget) {
        this.lerpRateOutwards = lerpRateOutwards;
        this.lerpRateInwards = lerpRateInwards;
        this.minDistanceAmount = minDistanceAmount;
        this.fixedAtMinDistance = fixedAtMinDistance;
        this.hasTarget = hasTarget;
    }

    public void lerpAiming(IsoPlayer player, Vector2 src, float targetX, float targetY, float centerX, float centerY, float zoom, Vector2 out_result) {
        boolean yFlipped;
        if (this.shouldSnapAimInFrontOfPlayer(player)) {
            AimingMode.snapAimInFrontOfPlayer(player, zoom, out_result);
            return;
        }
        if (this.fixedAtMinDistance) {
            this.aimFixedDistance(targetX, targetY, centerX, centerY, zoom, out_result);
            return;
        }
        float srcX = src.x;
        float srcY = src.y;
        if (PZMath.equal(srcX, targetX) && PZMath.equal(srcY, targetY)) {
            out_result.x = targetX;
            out_result.y = targetY;
            return;
        }
        float diffSrcX = srcX - centerX;
        float diffSrcY = srcY - centerY;
        float diffTargetX = targetX - centerX;
        float diffTargetY = targetY - centerY;
        boolean xFlipped = PZMath.sign(diffSrcX) != PZMath.sign(diffTargetX);
        boolean bl = yFlipped = PZMath.sign(diffSrcY) != PZMath.sign(diffTargetY);
        if (xFlipped && yFlipped) {
            out_result.x = targetX;
            out_result.y = targetY;
            return;
        }
        float distanceSrc = PZMath.sqrt(diffSrcX * diffSrcX + diffSrcY * diffSrcY);
        float distanceTarget = PZMath.sqrt(diffTargetX * diffTargetX + diffTargetY * diffTargetY);
        float aimingLerpRate = distanceTarget > distanceSrc ? this.lerpRateOutwards : this.lerpRateInwards;
        float aimingLerpRateClamped = PZMath.clamp(aimingLerpRate * GameTime.getInstance().getMultiplier(), 0.0f, 1.0f);
        out_result.x = PZMath.lerp(srcX, targetX, aimingLerpRateClamped, LerpType.Linear);
        out_result.y = PZMath.lerp(srcY, targetY, aimingLerpRateClamped, LerpType.Linear);
    }

    private boolean shouldSnapAimInFrontOfPlayer(IsoPlayer player) {
        return !player.isAimKeyDown() && player.isAiming() || this.fixedAtMinDistance && this.minDistanceAmount == -1.0f;
    }

    private void aimFixedDistance(float targetX, float targetY, float centerX, float centerY, float zoom, Vector2 out_result) {
        float diffX = targetX - centerX;
        float diffY = targetY - centerY;
        if (PZMath.equal(diffX, 0.0f) && PZMath.equal(diffY, 0.0f)) {
            out_result.x = targetX;
            out_result.y = targetY;
            return;
        }
        float diffDist = PZMath.sqrt(diffX * diffX + diffY * diffY);
        float dirX = diffX / diffDist;
        float dirY = diffY / diffDist;
        out_result.x = centerX * (1.0f + 2.0f * dirX * this.minDistanceAmount / zoom);
        out_result.y = centerY * (1.0f + 2.0f * dirY * this.minDistanceAmount / zoom);
    }

    private static void snapAimInFrontOfPlayer(IsoPlayer player, float zoom, Vector2 out_result) {
        Vector2 playerForward = player.getAnimForwardDirection(out_result);
        boolean isAimingRangedWeapon = player.isAimingFirearmEquipped();
        float characterAimPointHeight = isAimingRangedWeapon ? 0.468f : 0.57f;
        float playerTargetX = player.getX() + playerForward.x;
        float playerTargetY = player.getY() + playerForward.y;
        float playerTargetZ = player.getZ() + characterAimPointHeight;
        PlayerCamera camera = IsoCamera.cameras[player.getIndex()];
        out_result.x = camera.XToScreenExact(playerTargetX, playerTargetY, playerTargetZ, 0) / zoom;
        out_result.y = camera.YToScreenExact(playerTargetX, playerTargetY, playerTargetZ, 0) / zoom;
    }
}

