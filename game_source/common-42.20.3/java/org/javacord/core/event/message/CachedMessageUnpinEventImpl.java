/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.message;

import org.javacord.api.entity.message.Message;
import org.javacord.api.event.message.CachedMessageUnpinEvent;
import org.javacord.core.event.message.CertainMessageEventImpl;

public class CachedMessageUnpinEventImpl
extends CertainMessageEventImpl
implements CachedMessageUnpinEvent {
    public CachedMessageUnpinEventImpl(Message message) {
        super(message);
    }
}

