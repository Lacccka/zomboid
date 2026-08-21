/*
 * Decompiled with CFR 0.152.
 */
package zombie.characters.ecs;

import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import zombie.characters.component.FrameKeeperComponent;
import zombie.characters.ecs.ECSComponent;
import zombie.characters.ecs.componentmods.ECSFrameStep;
import zombie.characters.ecs.componentmods.ECSGameLoadingStateEnter;
import zombie.characters.ecs.componentmods.ECSInGameStateEnter;
import zombie.debug.DebugType;
import zombie.debug.LogSeverity;
import zombie.iso.IsoWorld;
import zombie.util.Type;

public interface ECSEntity {
    public HashMap<Class<? extends ECSComponent>, ECSComponent> getECSComponentMap();

    default public void registerECSComponents() {
        this.setECSComponent(new FrameKeeperComponent());
    }

    default public void frameStep() {
        int currentFrame = IsoWorld.instance.getFrameNo();
        FrameKeeperComponent ecsFrameKeeper = this.getFrameKeeper();
        if (ecsFrameKeeper.getFrameNo() == currentFrame) {
            DebugType.General.error("Double-update call to ECSEntity.frameStep, at frame %d. ECSEntities must only be updated once per frame.", currentFrame);
            DebugType.General.printStackTrace(LogSeverity.Error, -1, null, new Object[0]);
            throw new IllegalStateException(String.format("Double-update call at frame %d. ECSEntities must only be updated once per frame.", currentFrame));
        }
        ecsFrameKeeper.setFrameNo(currentFrame);
        this.visitAllComponents(ECSFrameStep.class, ECSFrameStep::frameStep);
    }

    private FrameKeeperComponent getFrameKeeper() {
        return this.getECSComponent(FrameKeeperComponent.class);
    }

    default public int getFrameNo() {
        return this.getFrameKeeper().getFrameNo();
    }

    default public void onInGameStateEnter() {
        this.visitAllComponents(ECSInGameStateEnter.class, ECSInGameStateEnter::onInGameStateEnter);
    }

    default public void onGameLoadingStateEnter() {
        this.visitAllComponents(ECSGameLoadingStateEnter.class, ECSGameLoadingStateEnter::onGameLoadingStateEnter);
    }

    default public <ComponentType extends ECSComponent> ComponentType getECSComponent(Class<ComponentType> componentTypeClass) {
        ECSEntity.checkParameterNotNull(componentTypeClass, "componentTypeClass");
        ComponentType foundComponentRaw = this.tryGetECSComponent(componentTypeClass);
        if (foundComponentRaw == null) {
            throw new IllegalStateException("Entity has no Component of type: " + componentTypeClass.getSimpleName());
        }
        return foundComponentRaw;
    }

    default public <ComponentType extends ECSComponent> void setECSComponent(ComponentType component) {
        ECSEntity.checkParameterNotNull(component, "component");
        if (!this.hasECSComponent(component)) {
            this.removeECSComponent(component.getECSClass());
            this.getECSComponentMapInternal().put(component.getECSClass(), component);
            component.setECSOwnerEntity((ECSEntity)this);
        }
    }

    default public <ComponentType extends ECSComponent> void removeECSComponent(ComponentType component) {
        ECSEntity.checkParameterNotNull(component, "component");
        if (this.hasECSComponent(component)) {
            this.removeECSComponent(component.getECSClass());
        }
    }

    default public <ComponentType extends ECSComponent> void removeECSComponent(Class<ComponentType> componentClass) {
        ECSEntity.checkParameterNotNull(componentClass, "componentClass");
        ComponentType component = this.tryGetECSComponent(componentClass);
        if (component != null) {
            this.getECSComponentMapInternal().remove(((ECSComponent)component).getECSClass());
            ((ECSComponent)component).setECSOwnerEntity(null);
        }
    }

    default public <ComponentType extends ECSComponent> ComponentType tryGetECSComponent(Class<ComponentType> componentTypeClass) {
        ECSEntity.checkParameterNotNull(componentTypeClass, "componentTypeClass");
        return (ComponentType)((ECSComponent)Type.tryCastTo(this.getECSComponentMapInternal().get(ECSComponent.getECSClass(componentTypeClass)), componentTypeClass));
    }

    default public boolean hasECSComponent(Class<? extends ECSComponent> componentTypeClass) {
        ECSEntity.checkParameterNotNull(componentTypeClass, "componentTypeClass");
        return this.tryGetECSComponent(componentTypeClass) != null;
    }

    default public boolean hasECSComponent(ECSComponent component) {
        ECSEntity.checkParameterNotNull(component, "component");
        return this.tryGetECSComponent(component.getECSClass()) == component;
    }

    default public <ST> void visitAllComponents(Class<? extends ST> instanceOf, Consumer<ST> visitor) {
        for (ECSComponent c : this.getECSComponentMapInternal().values()) {
            ST converted = Type.tryCastTo(c, instanceOf);
            if (converted == null) continue;
            visitor.accept(converted);
        }
    }

    default public <ST, P1> void visitAllComponents(Class<? extends ST> instanceOf, BiConsumer<ST, P1> visitor, P1 param1) {
        for (ECSComponent c : this.getECSComponentMapInternal().values()) {
            ST converted = Type.tryCastTo(c, instanceOf);
            if (converted == null) continue;
            visitor.accept(converted, param1);
        }
    }

    private HashMap<Class<? extends ECSComponent>, ECSComponent> getECSComponentMapInternal() {
        HashMap<Class<? extends ECSComponent>, ECSComponent> componentMap = this.getECSComponentMap();
        if (componentMap == null) {
            throw new IllegalStateException("Entity has no component map.");
        }
        return componentMap;
    }

    public static void checkParameterNotNull(Object parameter, String parameterName) {
        if (parameter == null) {
            throw new IllegalArgumentException("Parameter " + parameterName + " cannot be null.");
        }
    }
}

