/*
 * Decompiled with CFR 0.152.
 */
package zombie.input;

import java.lang.invoke.LambdaMetafactory;
import java.util.function.BiFunction;
import zombie.characters.CharacterInputMode;
import zombie.characters.IsoPlayer;
import zombie.characters.component.CharacterInputComponent;
import zombie.core.Core;
import zombie.core.math.PZMath;
import zombie.core.math.interpolators.LerpType;
import zombie.input.AimingMode;
import zombie.input.Mouse;
import zombie.iso.IsoCamera;
import zombie.iso.Vector2;
import zombie.util.lambda.PZOptional;
import zombie.util.list.PZArrayUtil;

public class AimingReticle {
    public static final float AIMING_AXIS_MIN = 0.2f;
    public static final float AIMING_AXIS_MAX = 0.99f;
    public static final float AIMING_VALUE_MIN = 0.01f;
    public static final float AIMING_VALUE_MAX = 0.98f;
    private static final Vector2[] aimingPositions = PZArrayUtil.newInstance(Vector2.class, 4, Vector2::new);
    private static final Vector2 axisVec = new Vector2();

    public static int getX(int playerIndex) {
        return (int)((float)AimingReticle.getXA(playerIndex) * Core.getInstance().getZoom(playerIndex));
    }

    public static int getY(int playerIndex) {
        return (int)((float)AimingReticle.getYA(playerIndex) * Core.getInstance().getZoom(playerIndex));
    }

    public static int getXA(int playerIndex) {
        IsoPlayer player = IsoPlayer.getPlayer(playerIndex);
        if (player == null || player.getInputMode() != CharacterInputMode.GAMEPAD) {
            return Mouse.getXA();
        }
        return (int)AimingReticle.aimingPositions[playerIndex].x;
    }

    public static int getYA(int playerIndex) {
        IsoPlayer player = IsoPlayer.getPlayer(playerIndex);
        if (player == null || player.getInputMode() != CharacterInputMode.GAMEPAD) {
            return Mouse.getYA();
        }
        return (int)AimingReticle.aimingPositions[playerIndex].y;
    }

    private static float axisToScreenX(IsoPlayer player, float aimingX) {
        float screenHalfWidth = AimingReticle.getAimingCenterX(player);
        return screenHalfWidth * (1.0f + aimingX);
    }

    private static float axisToScreenY(IsoPlayer player, float aimingY) {
        float screenHalfHeight = AimingReticle.getAimingCenterY(player);
        return screenHalfHeight * (1.0f + aimingY);
    }

    private static float renormalizeAimingAxisValue(IsoPlayer player, float aimingAxisVal) {
        float zoom = Core.getInstance().getZoom(player.getIndex());
        float aimingAxisSign = PZMath.sign(aimingAxisVal);
        float renormalizedAxis = PZMath.clamp((aimingAxisVal * aimingAxisSign - 0.2f) / 0.79f, 0.0f, 1.0f) * aimingAxisSign;
        return PZMath.lerp(0.01f / zoom, 0.98f, renormalizedAxis * aimingAxisSign, LerpType.EaseOutQuad) * aimingAxisSign;
    }

    private static float getAimingScreenX(IsoPlayer player) {
        float aimingAxisRaw = PZOptional.ifPresent(player.tryGetECSComponent(CharacterInputComponent.class), AimingReticle.axisVec.set((float)0.0f, (float)0.0f), (BiFunction<CharacterInputComponent, Vector2, Vector2>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;, getJoypadAimVector(zombie.iso.Vector2 ), (Lzombie/characters/component/CharacterInputComponent;Lzombie/iso/Vector2;)Lzombie/iso/Vector2;)(), AimingReticle.axisVec).x;
        float aimingAxis = AimingReticle.renormalizeAimingAxisValue(player, aimingAxisRaw);
        return AimingReticle.axisToScreenX(player, aimingAxis);
    }

    private static float getAimingScreenY(IsoPlayer player) {
        float aimingAxisRaw = PZOptional.ifPresent(player.tryGetECSComponent(CharacterInputComponent.class), AimingReticle.axisVec.set((float)0.0f, (float)0.0f), (BiFunction<CharacterInputComponent, Vector2, Vector2>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;, getJoypadAimVector(zombie.iso.Vector2 ), (Lzombie/characters/component/CharacterInputComponent;Lzombie/iso/Vector2;)Lzombie/iso/Vector2;)(), AimingReticle.axisVec).y;
        float aimingAxis = AimingReticle.renormalizeAimingAxisValue(player, aimingAxisRaw);
        return AimingReticle.axisToScreenY(player, aimingAxis);
    }

    private static float getAimingCenterX(IsoPlayer player) {
        int playerIndex = player.getIndex();
        int screenWidth = IsoCamera.getScreenWidth(playerIndex);
        return (float)screenWidth / 2.0f;
    }

    private static float getAimingCenterY(IsoPlayer player) {
        int playerIndex = player.getIndex();
        int screenHeight = IsoCamera.getScreenHeight(playerIndex);
        return (float)screenHeight / 2.0f - player.getScreenChestHeight();
    }

    public static void update() {
        for (int i = 0; i < aimingPositions.length; ++i) {
            IsoPlayer player = IsoPlayer.getPlayer(i);
            if (player == null || player.getInputMode() != CharacterInputMode.GAMEPAD) continue;
            AimingMode aimingMode = player.getAimingMode();
            aimingMode.lerpAiming(player, aimingPositions[i], AimingReticle.getAimingScreenX(player), AimingReticle.getAimingScreenY(player), AimingReticle.getAimingCenterX(player), AimingReticle.getAimingCenterY(player), Core.getInstance().getZoom(i), aimingPositions[i]);
        }
    }
}

