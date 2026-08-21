/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server.role;

import java.awt.Color;
import java.util.Optional;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.event.server.role.RoleChangeColorEvent;
import org.javacord.core.event.server.role.RoleEventImpl;

public class RoleChangeColorEventImpl
extends RoleEventImpl
implements RoleChangeColorEvent {
    private final Color newColor;
    private final Color oldColor;

    public RoleChangeColorEventImpl(Role role, Color newColor, Color oldColor) {
        super(role);
        this.newColor = newColor;
        this.oldColor = oldColor;
    }

    @Override
    public Optional<Color> getOldColor() {
        return Optional.ofNullable(this.oldColor);
    }

    @Override
    public Optional<Color> getNewColor() {
        return Optional.ofNullable(this.newColor);
    }
}

