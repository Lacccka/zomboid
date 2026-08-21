/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware;

import java.util.Map;
import java.util.Set;
import oshi.annotation.concurrent.Immutable;

@Immutable
public interface LogicalVolumeGroup {
    public String getName();

    public Set<String> getPhysicalVolumes();

    public Map<String, Set<String>> getLogicalVolumes();
}

