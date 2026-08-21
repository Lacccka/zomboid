/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.message.component;

import java.util.Optional;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.component.Component;
import org.javacord.api.entity.message.component.ComponentType;
import org.javacord.api.entity.message.component.SelectMenu;
import org.javacord.api.entity.message.component.TextInput;
import org.javacord.api.util.Specializable;

public interface LowLevelComponent
extends Component,
Specializable<LowLevelComponent> {
    default public boolean isButton() {
        return this.getType() == ComponentType.BUTTON;
    }

    default public Optional<Button> asButton() {
        return this.isButton() ? Optional.of((Button)this) : Optional.empty();
    }

    default public boolean isSelectMenu() {
        return this.getType().isSelectMenuType();
    }

    default public Optional<SelectMenu> asSelectMenu() {
        return this.isSelectMenu() ? Optional.of((SelectMenu)this) : Optional.empty();
    }

    default public boolean isTextInput() {
        return this.getType() == ComponentType.TEXT_INPUT;
    }

    default public Optional<TextInput> asTextInput() {
        return this.isTextInput() ? Optional.of((TextInput)this) : Optional.empty();
    }
}

