/*
 * Decompiled with CFR 0.152.
 */
package zombie.characters.ecs;

import zombie.characters.ecs.ECSEntity;
import zombie.util.Type;

public abstract class ECSComponent {
    private final Class<? extends ECSComponent> ecsClass = ECSComponent.getECSClass(this.getClass());
    private ECSEntity ecsOwner;

    public Class<? extends ECSComponent> getECSClass() {
        return this.ecsClass;
    }

    public static Class<? extends ECSComponent> getECSClass(Class<? extends ECSComponent> clazz) {
        Class<? extends ECSComponent> foundEcsClass = null;
        for (Class<? extends ECSComponent> c = clazz; c != null && c != ECSComponent.class; c = c.getSuperclass()) {
            foundEcsClass = c;
        }
        return foundEcsClass;
    }

    public ECSEntity getECSOwnerEntity() {
        return this.ecsOwner;
    }

    public <EntityType extends ECSEntity> void setECSOwnerEntity(EntityType ownerEntity) {
        if (this.ecsOwner == ownerEntity) {
            return;
        }
        ECSEntity prevOwner = this.ecsOwner;
        this.ecsOwner = ownerEntity;
        if (prevOwner != null) {
            prevOwner.removeECSComponent(this);
        }
        if (this.ecsOwner != null) {
            this.ecsOwner.setECSComponent(this);
        }
    }

    public <EntityType extends ECSEntity> EntityType getECSOwnerEntity(Class<EntityType> entityTypeClass) {
        return (EntityType)((ECSEntity)entityTypeClass.cast(this.getECSOwnerEntity()));
    }

    public <EntityType extends ECSEntity> EntityType tryGetECSOwnerEntity(Class<EntityType> entityTypeClass) {
        return (EntityType)((ECSEntity)Type.tryCastTo(this.getECSOwnerEntity(), entityTypeClass));
    }

    public <OwnerType> OwnerType tryGetECSOwnerEntityAs(Class<? extends OwnerType> ownerTypeClass) {
        return Type.tryCastTo(this.getECSOwnerEntity(), ownerTypeClass);
    }
}

