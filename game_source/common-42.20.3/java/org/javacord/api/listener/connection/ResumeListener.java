/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.connection;

import org.javacord.api.event.connection.ResumeEvent;
import org.javacord.api.listener.GloballyAttachableListener;

@FunctionalInterface
public interface ResumeListener
extends GloballyAttachableListener {
    public void onResume(ResumeEvent var1);
}

