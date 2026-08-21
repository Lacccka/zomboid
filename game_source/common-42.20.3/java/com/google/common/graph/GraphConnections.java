/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.errorprone.annotations.CanIgnoreReturnValue
 *  javax.annotation.Nullable
 */
package com.google.common.graph;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Set;
import javax.annotation.Nullable;

interface GraphConnections<N, V> {
    public Set<N> adjacentNodes();

    public Set<N> predecessors();

    public Set<N> successors();

    @Nullable
    public V value(N var1);

    public void removePredecessor(N var1);

    @CanIgnoreReturnValue
    public V removeSuccessor(N var1);

    public void addPredecessor(N var1, V var2);

    @CanIgnoreReturnValue
    public V addSuccessor(N var1, V var2);
}

