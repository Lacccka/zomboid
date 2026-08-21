/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.interaction;

import org.javacord.api.event.Event;
import org.javacord.api.interaction.Interaction;

public interface ApplicationCommandEvent
extends Event {
    public Interaction getInteraction();
}

