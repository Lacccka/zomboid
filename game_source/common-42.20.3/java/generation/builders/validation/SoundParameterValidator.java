/*
 * Decompiled with CFR 0.152.
 */
package generation.builders.validation;

import java.util.Map;
import zombie.audio.parameters.ParameterBroadcastGenre;
import zombie.audio.parameters.ParameterBroadcastVoiceType;
import zombie.audio.parameters.ParameterBulletHitSurface;
import zombie.audio.parameters.ParameterCharacterMovementSpeed;
import zombie.audio.parameters.ParameterEquippedBaggageContainer;
import zombie.audio.parameters.ParameterMeleeHitSurface;
import zombie.audio.parameters.ParameterVehicleHitLocation;
import zombie.audio.parameters.ParameterZombieState;

public record SoundParameterValidator(String name, Object value) {
    public static final Map<String, Class<? extends Enum<?>>> PARAMETERS = Map.ofEntries(Map.entry("BroadcastGenre", ParameterBroadcastGenre.BroadcastGenre.class), Map.entry("BroadcastVoiceType", ParameterBroadcastVoiceType.BroadcastVoiceType.class), Map.entry("BulletHitSurface", ParameterBulletHitSurface.Material.class), Map.entry("CharacterMovementSpeed", ParameterCharacterMovementSpeed.MovementType.class), Map.entry("EquippedBaggageContainer", ParameterEquippedBaggageContainer.ContainerType.class), Map.entry("MeleeHitSurface", ParameterMeleeHitSurface.Material.class), Map.entry("VehicleHitLocation", ParameterVehicleHitLocation.HitLocation.class), Map.entry("ZombieState", ParameterZombieState.State.class));

    public static <T extends Enum<T>> SoundParameterValidator of(String key, T value) {
        if (PARAMETERS.containsKey(key)) {
            Class<? extends Enum<?>> aClass = PARAMETERS.get(key);
            if (value.getClass().equals(aClass)) {
                return new SoundParameterValidator(key, value);
            }
        }
        throw new RuntimeException("Unknown SoundParameter: " + key);
    }

    @Override
    public String toString() {
        return "%s %s".formatted(this.name, this.value);
    }
}

