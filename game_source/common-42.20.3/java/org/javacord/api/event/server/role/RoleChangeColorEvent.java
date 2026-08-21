/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.server.role;

import java.awt.Color;
import java.util.Optional;
import org.javacord.api.event.server.role.RoleEvent;

public interface RoleChangeColorEvent
extends RoleEvent {
    public Optional<Color> getOldColor();

    public Optional<Color> getNewColor();
}

