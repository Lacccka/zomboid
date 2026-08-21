/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractDynamicOrderPropertyBuilder;
import generation.builders.ComponentBuilder;
import generation.builders.FluidContainerFluidBuilder;
import generation.builders.Writeable;
import java.io.IOException;
import java.io.Writer;
import zombie.scripting.objects.FluidKey;
import zombie.scripting.objects.SoundKey;

public class ComponentFluidContainerBuilder
extends AbstractDynamicOrderPropertyBuilder
implements ComponentBuilder {
    private final Writeable.Property<String> containerName = this.property("ContainerName");
    private final Writeable.Property<Float> capacity = this.property("Capacity");
    private final Writeable.Property<Float> rainFactor = this.property("RainFactor");
    private final Writeable.Property<Float> initialPercentMin = this.property("InitialPercentMin");
    private final Writeable.Property<Float> initialPercentMax = this.property("InitialPercentMax");
    private final Writeable.Property<Boolean> inputLocked = this.property("InputLocked");
    private final Writeable.Property<Boolean> fillsWithCleanWater = this.property("FillsWithCleanWater");
    private final Writeable.ListProperty<WhiteListFluid> whitelist = this.listProperty("whitelist", new Writeable.ListProperty.Flags[0]);
    private final Writeable.Property<Boolean> hiddenAmount = this.property("HiddenAmount");
    private final Writeable.Property<SoundKey> customDrinkSound = this.property("CustomDrinkSound");
    private final Writeable.Property<Boolean> pickRandomFluid = this.property("PickRandomFluid");
    private final Writeable.Property<Boolean> opened = this.property("Opened");
    private final Writeable.ListProperty<FluidContainerFluidBuilder> fluids = this.listProperty("Fluids", Writeable.ListProperty.Flags.SHOW_IF_EMPTY);
    private final Writeable.Property<Float> transferRate = this.property("TransferRate");

    public ComponentFluidContainerBuilder() {
        super("FluidContainer");
    }

    @Override
    public String getType() {
        return this.getName();
    }

    public ComponentFluidContainerBuilder containerName(String containerName) {
        this.containerName.setValue(containerName);
        return this;
    }

    public ComponentFluidContainerBuilder capacity(float capacity) {
        this.capacity.setValue(Float.valueOf(capacity));
        return this;
    }

    public ComponentFluidContainerBuilder rainFactor(float rainFactor) {
        this.rainFactor.setValue(Float.valueOf(rainFactor));
        return this;
    }

    public ComponentFluidContainerBuilder initialPercentMin(float initialPercentMin) {
        this.initialPercentMin.setValue(Float.valueOf(initialPercentMin));
        return this;
    }

    public ComponentFluidContainerBuilder initialPercentMax(float initialPercentMax) {
        this.initialPercentMax.setValue(Float.valueOf(initialPercentMax));
        return this;
    }

    public ComponentFluidContainerBuilder inputLocked(boolean inputLocked) {
        this.inputLocked.setValue(inputLocked);
        return this;
    }

    public ComponentFluidContainerBuilder fillsWithCleanWater(boolean fillsWithCleanWater) {
        this.fillsWithCleanWater.setValue(fillsWithCleanWater);
        return this;
    }

    public ComponentFluidContainerBuilder whitelist(FluidKey whitelist) {
        this.whitelist.addValues((WhiteListFluid[])new WhiteListFluid[]{new WhiteListFluid(whitelist)});
        return this;
    }

    public ComponentFluidContainerBuilder hiddenAmount(boolean hiddenAmount) {
        this.hiddenAmount.setValue(hiddenAmount);
        return this;
    }

    public ComponentFluidContainerBuilder customDrinkSound(SoundKey customDrinkSound) {
        this.customDrinkSound.setValue(customDrinkSound);
        return this;
    }

    public ComponentFluidContainerBuilder pickRandomFluid(boolean pickRandomFluid) {
        this.pickRandomFluid.setValue(pickRandomFluid);
        return this;
    }

    public ComponentFluidContainerBuilder opened(boolean opened) {
        this.opened.setValue(opened);
        return this;
    }

    public ComponentFluidContainerBuilder fluids(FluidContainerFluidBuilder ... fluids) {
        this.fluids.addValues((FluidContainerFluidBuilder[])fluids);
        return this;
    }

    public ComponentFluidContainerBuilder transferRate(float transferRate) {
        this.transferRate.setValue(Float.valueOf(transferRate));
        return this;
    }

    private record WhiteListFluid(FluidKey fluidKey) implements Writeable
    {
        @Override
        public void write(Writer writer, int indent, String key) throws IOException {
            this.writeKeyValue(writer, indent, "fluid", this.fluidKey);
        }
    }
}

