/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system.ffm;

import java.lang.foreign.SegmentAllocator;

public interface StackAllocator<T extends StackAllocator<T>>
extends SegmentAllocator {
    public T push();

    public T pop();
}

