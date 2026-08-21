/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.message;

import org.javacord.api.entity.message.Message;
import org.javacord.api.event.message.CachedMessagePinEvent;
import org.javacord.core.event.message.CertainMessageEventImpl;

public class CachedMessagePinEventImpl
extends CertainMessageEventImpl
implements CachedMessagePinEvent {
    public CachedMessagePinEventImpl(Message message) {
        super(message);
    }
}

