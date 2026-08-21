/*
 * Decompiled with CFR 0.152.
 */
package zombie.vehicles;

import zombie.characters.IsoGameCharacter;
import zombie.util.Pool;
import zombie.util.PooledObject;

public class VehiclePedestrianContact
extends PooledObject {
    public IsoGameCharacter character;
    public float hitTime;
    private static final Pool<VehiclePedestrianContact> POOL = new Pool<VehiclePedestrianContact>(VehiclePedestrianContact::new);

    private VehiclePedestrianContact() {
    }

    @Override
    public void onReleased() {
        this.character = null;
        this.hitTime = 0.0f;
    }

    public boolean isCharacter(IsoGameCharacter character) {
        return this.character == character;
    }

    public static VehiclePedestrianContact alloc() {
        return POOL.alloc();
    }
}

