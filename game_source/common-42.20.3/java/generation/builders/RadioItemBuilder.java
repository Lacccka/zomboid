/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.ItemBuilder;
import generation.builders.Writeable;
import zombie.scripting.objects.ItemKey;

public class RadioItemBuilder
extends ItemBuilder<RadioItemBuilder> {
    private final Writeable.Property<Integer> acceptMediaType = this.property("AcceptMediaType");
    private final Writeable.Property<Integer> baseVolumeRange = this.property("BaseVolumeRange");
    private final Writeable.Property<Boolean> isHighTier = this.property("IsHighTier");
    private final Writeable.Property<Boolean> isPortable = this.property("IsPortable");
    private final Writeable.Property<Boolean> isTelevision = this.property("IsTelevision");
    private final Writeable.Property<Integer> maxChannel = this.property("MaxChannel");
    private final Writeable.Property<Integer> micRange = this.property("MicRange");
    private final Writeable.Property<Integer> minChannel = this.property("MinChannel");
    private final Writeable.Property<Boolean> noTransmit = this.property("NoTransmit");
    private final Writeable.Property<Integer> transmitRange = this.property("TransmitRange");
    private final Writeable.Property<Boolean> twoWay = this.property("TwoWay");
    private final Writeable.Property<Float> useDelta = this.property("UseDelta", this::formatFloat);
    private final Writeable.Property<Boolean> usesBattery = this.property("UsesBattery");

    public RadioItemBuilder(ItemKey item) {
        super(item);
    }

    public RadioItemBuilder acceptMediaType(int acceptMediaType) {
        this.acceptMediaType.setValue(acceptMediaType);
        return this;
    }

    public RadioItemBuilder baseVolumeRange(int baseVolumeRange) {
        this.baseVolumeRange.setValue(baseVolumeRange);
        return this;
    }

    public RadioItemBuilder isHighTier(boolean isHighTier) {
        this.isHighTier.setValue(isHighTier);
        return this;
    }

    public RadioItemBuilder isPortable(boolean isPortable) {
        this.isPortable.setValue(isPortable);
        return this;
    }

    public RadioItemBuilder isTelevision(boolean isTelevision) {
        this.isTelevision.setValue(isTelevision);
        return this;
    }

    public RadioItemBuilder maxChannel(int maxChannel) {
        this.maxChannel.setValue(maxChannel);
        return this;
    }

    public RadioItemBuilder micRange(int micRange) {
        this.micRange.setValue(micRange);
        return this;
    }

    public RadioItemBuilder minChannel(int minChannel) {
        this.minChannel.setValue(minChannel);
        return this;
    }

    public RadioItemBuilder noTransmit(boolean noTransmit) {
        this.noTransmit.setValue(noTransmit);
        return this;
    }

    public RadioItemBuilder transmitRange(int transmitRange) {
        this.transmitRange.setValue(transmitRange);
        return this;
    }

    public RadioItemBuilder twoWay(boolean twoWay) {
        this.twoWay.setValue(twoWay);
        return this;
    }

    public RadioItemBuilder useDelta(float useDelta) {
        this.useDelta.setValue(Float.valueOf(useDelta));
        return this;
    }

    public RadioItemBuilder usesBattery(boolean usesBattery) {
        this.usesBattery.setValue(usesBattery);
        return this;
    }
}

