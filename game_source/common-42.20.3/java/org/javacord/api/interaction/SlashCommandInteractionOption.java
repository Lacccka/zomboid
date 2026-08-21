/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.interaction;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.Attachment;
import org.javacord.api.entity.Mentionable;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.SlashCommandInteractionOptionsProvider;

public interface SlashCommandInteractionOption
extends SlashCommandInteractionOptionsProvider {
    public String getName();

    public Optional<Boolean> isFocused();

    default public boolean isSubcommandOrGroup() {
        return !this.getStringRepresentationValue().isPresent();
    }

    public Optional<String> getStringRepresentationValue();

    public Optional<String> getStringValue();

    public Optional<Long> getLongValue();

    public Optional<Boolean> getBooleanValue();

    public Optional<User> getUserValue();

    public Optional<CompletableFuture<User>> requestUserValue();

    public Optional<ServerChannel> getChannelValue();

    public Optional<Attachment> getAttachmentValue();

    public Optional<Role> getRoleValue();

    public Optional<Mentionable> getMentionableValue();

    public Optional<Double> getDecimalValue();

    public Optional<CompletableFuture<Mentionable>> requestMentionableValue();

    @Override
    public List<SlashCommandInteractionOption> getOptions();
}

