/*
 * Decompiled with CFR 0.152.
 */
package zombie.characters;

import zombie.UsedFromLua;
import zombie.characters.CharacterInputMode;
import zombie.characters.component.CharacterInputComponent;
import zombie.characters.ecs.ECSEntity;
import zombie.iso.Vector2;
import zombie.util.lambda.PZOptional;

public interface CharacterInputComponentEntity
extends ECSEntity {
    default public CharacterInputComponent getCharacterInputComponent() {
        return this.tryGetECSComponent(CharacterInputComponent.class);
    }

    default public int getJoypadBind() {
        return PZOptional.ifPresent(this.getCharacterInputComponent(), -1, CharacterInputComponent::getJoypadBind);
    }

    default public void setJoypadBind(int joypadBind) {
        PZOptional.ifPresent(this.getCharacterInputComponent(), CharacterInputComponent::setJoypadBind, joypadBind);
    }

    default public CharacterInputMode getInputMode() {
        return PZOptional.ifPresent(this.getCharacterInputComponent(), CharacterInputMode.NONE, CharacterInputComponent::getInputMode);
    }

    default public boolean isForceAim() {
        return PZOptional.ifPresent(this.getCharacterInputComponent(), CharacterInputComponent::isForceAim);
    }

    default public void setForceAim(boolean forceAim) {
        PZOptional.ifPresent(this.getCharacterInputComponent(), CharacterInputComponent::setForceAim, forceAim);
    }

    default public boolean toggleForceAim() {
        return PZOptional.ifPresent(this.getCharacterInputComponent(), CharacterInputComponent::toggleForceAim);
    }

    default public boolean isForceSprint() {
        return PZOptional.ifPresent(this.getCharacterInputComponent(), CharacterInputComponent::isForceSprint);
    }

    default public void setForceSprint(boolean forceSprint) {
        PZOptional.ifPresent(this.getCharacterInputComponent(), CharacterInputComponent::setForceSprint, forceSprint);
    }

    default public boolean isForceRun() {
        return PZOptional.ifPresent(this.getCharacterInputComponent(), CharacterInputComponent::isForceRun);
    }

    default public void setForceRun(boolean forceRun) {
        PZOptional.ifPresent(this.getCharacterInputComponent(), CharacterInputComponent::setForceRun, forceRun);
    }

    default public Vector2 getInputMoveVector(Vector2 out) {
        return PZOptional.ifPresent(this.getCharacterInputComponent(), out.set(0.0f, 0.0f), CharacterInputComponent::getInputMoveVector, out);
    }

    default public float getInputMovementRate() {
        return PZOptional.ifPresent(this.getCharacterInputComponent(), Float.valueOf(0.0f), CharacterInputComponent::getInputMovementRate).floatValue();
    }

    default public boolean isInputMoveAxisApplied() {
        return PZOptional.ifPresent(this.getCharacterInputComponent(), false, CharacterInputComponent::isInputMoveAxisApplied);
    }

    default public boolean isAimKeyDown() {
        return PZOptional.ifPresent(this.getCharacterInputComponent(), CharacterInputComponent::isAimKeyDown);
    }

    default public boolean isPrecisionAimKeyDown() {
        return PZOptional.ifPresent(this.getCharacterInputComponent(), CharacterInputComponent::isPrecisionAimKeyDown);
    }

    default public boolean isAnyAimKeyDown() {
        return PZOptional.ifPresent(this.getCharacterInputComponent(), CharacterInputComponent::isAnyAimKeyDown);
    }

    default public boolean isMeleeButtonDown() {
        return PZOptional.ifPresent(this.getCharacterInputComponent(), CharacterInputComponent::isMeleeButtonDown);
    }

    default public boolean isAttackButtonDown() {
        return PZOptional.ifPresent(this.getCharacterInputComponent(), CharacterInputComponent::isAttackButtonDown);
    }

    default public boolean isBuildButtonDown() {
        return PZOptional.ifPresent(this.getCharacterInputComponent(), CharacterInputComponent::isBuildButtonDown);
    }

    default public boolean isBuildButtonReleased() {
        return PZOptional.ifPresent(this.getCharacterInputComponent(), CharacterInputComponent::isBuildButtonReleased);
    }

    default public boolean isRunButtonDown() {
        return PZOptional.ifPresent(this.getCharacterInputComponent(), CharacterInputComponent::isRunButtonDown);
    }

    default public boolean wasRunButtonDown() {
        return PZOptional.ifPresent(this.getCharacterInputComponent(), CharacterInputComponent::wasRunButtonDown);
    }

    default public boolean isInteractButtonPressed() {
        return PZOptional.ifPresent(this.getCharacterInputComponent(), CharacterInputComponent::isInteractButtonPressed);
    }

    default public boolean isInteractButtonDown() {
        return PZOptional.ifPresent(this.getCharacterInputComponent(), CharacterInputComponent::isInteractButtonDown);
    }

    default public boolean isInteractButtonClicked() {
        return PZOptional.ifPresent(this.getCharacterInputComponent(), CharacterInputComponent::isInteractButtonClicked);
    }

    default public boolean isWalkToButtonDown() {
        return PZOptional.ifPresent(this.getCharacterInputComponent(), CharacterInputComponent::isWalkToButtonDown);
    }

    default public boolean isCrouchButtonPressed() {
        return PZOptional.ifPresent(this.getCharacterInputComponent(), CharacterInputComponent::isCrouchButtonPressed);
    }

    default public boolean isSprintButtonDown() {
        return PZOptional.ifPresent(this.getCharacterInputComponent(), CharacterInputComponent::isSprintButtonDown);
    }

    default public boolean isManualFloorAtkButtonDown() {
        return PZOptional.ifPresent(this.getCharacterInputComponent(), CharacterInputComponent::isManualFloorAtkButtonDown);
    }

    default public boolean isShiftKeyDown() {
        return PZOptional.ifPresent(this.getCharacterInputComponent(), CharacterInputComponent::isShiftKeyDown);
    }

    default public boolean isF12KeyDown() {
        return PZOptional.ifPresent(this.getCharacterInputComponent(), CharacterInputComponent::isF12KeyDown);
    }

    default public boolean isChangeCharacterKeyDown() {
        return PZOptional.ifPresent(this.getCharacterInputComponent(), CharacterInputComponent::isChangeCharacterKeyDown);
    }

    default public void setJoypadButtonsActive(boolean joypadMovementActive) {
        PZOptional.ifPresent(this.getCharacterInputComponent(), CharacterInputComponent::setJoypadButtonsActive, joypadMovementActive);
    }

    default public boolean isJoypadButtonsActive() {
        return PZOptional.ifPresent(this.getCharacterInputComponent(), CharacterInputComponent::isJoypadButtonsActive);
    }

    @UsedFromLua
    default public void setIgnoreInputsForDirection(boolean ignoreInputsForDirection) {
        PZOptional.ifPresent(this.getCharacterInputComponent(), CharacterInputComponent::setIgnoreInputsForDirection, ignoreInputsForDirection);
    }

    @UsedFromLua
    default public boolean isIgnoreInputsForDirection() {
        return PZOptional.ifPresent(this.getCharacterInputComponent(), CharacterInputComponent::isIgnoreInputsForDirection);
    }

    @UsedFromLua
    default public void setJoypadIgnoreAim(boolean ignore) {
        PZOptional.ifPresent(this.getCharacterInputComponent(), CharacterInputComponent::setJoypadIgnoreAim, ignore);
    }

    @UsedFromLua
    default public void setJoypadIgnoreAimUntilCentered(boolean ignore) {
        PZOptional.ifPresent(this.getCharacterInputComponent(), CharacterInputComponent::setJoypadIgnoreAimUntilCentered, ignore);
    }

    @UsedFromLua
    default public boolean isJoypadIgnoreAimUntilCentered() {
        return PZOptional.ifPresent(this.getCharacterInputComponent(), CharacterInputComponent::isJoypadIgnoreAimUntilCentered);
    }

    default public void setIgnoreAimingInput(boolean b) {
        PZOptional.ifPresent(this.getCharacterInputComponent(), CharacterInputComponent::setIgnoreAimingInput, b);
    }

    default public boolean isIgnoringAimingInput() {
        return PZOptional.ifPresent(this.getCharacterInputComponent(), CharacterInputComponent::isIgnoringAimingInput);
    }

    default public boolean isAllowSprint() {
        return PZOptional.ifPresent(this.getCharacterInputComponent(), false, CharacterInputComponent::isAllowSprint);
    }

    default public void setAllowSprint(boolean allowSprint) {
        PZOptional.ifPresent(this.getCharacterInputComponent(), CharacterInputComponent::setAllowSprint, allowSprint);
    }

    default public boolean isAllowRun() {
        return PZOptional.ifPresent(this.getCharacterInputComponent(), false, CharacterInputComponent::isAllowRun);
    }

    default public void setAllowRun(boolean allowRun) {
        PZOptional.ifPresent(this.getCharacterInputComponent(), CharacterInputComponent::setAllowRun, allowRun);
    }
}

