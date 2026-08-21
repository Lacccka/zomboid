/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system.ffm;

import org.lwjgl.system.ffm.GroupBinder;
import org.lwjgl.system.ffm.UpcallBinder;

sealed interface Binder<T>
permits GroupBinder, UpcallBinder {
}

