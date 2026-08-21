/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.interaction;

import java.util.List;
import java.util.Optional;
import org.javacord.api.entity.Mentionable;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.message.component.SelectMenuOption;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.MessageComponentInteraction;

public interface SelectMenuInteraction
extends MessageComponentInteraction {
    public List<Role> getSelectedRoles();

    public List<User> getSelectedUsers();

    public List<ServerChannel> getSelectedChannels();

    public List<Mentionable> getSelectedMentionables();

    public List<SelectMenuOption> getChosenOptions();

    public List<SelectMenuOption> getPossibleOptions();

    @Override
    public String getCustomId();

    public Optional<String> getPlaceholder();

    public int getMinimumValues();

    public int getMaximumValues();
}

