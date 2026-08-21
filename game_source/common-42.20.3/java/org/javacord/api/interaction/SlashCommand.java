/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.interaction;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import org.javacord.api.entity.Mentionable;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.interaction.ApplicationCommand;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.SlashCommandOptionBuilder;
import org.javacord.api.interaction.SlashCommandUpdater;

public interface SlashCommand
extends ApplicationCommand,
Mentionable {
    @Override
    default public String getMentionTag() {
        return "</" + this.getName() + ":" + this.getId() + ">";
    }

    default public List<String> getMentionTags() {
        return this.getFullCommandNames().stream().map(name -> "</" + name + ":" + this.getId() + ">").collect(Collectors.toList());
    }

    public List<String> getFullCommandNames();

    public List<SlashCommandOption> getOptions();

    public static SlashCommandBuilder with(String name, String description) {
        return (SlashCommandBuilder)((SlashCommandBuilder)new SlashCommandBuilder().setName(name)).setDescription(description);
    }

    public static SlashCommandBuilder with(String name, String description, SlashCommandOptionBuilder ... options) {
        return SlashCommand.with(name, description, Arrays.stream(options).map(SlashCommandOptionBuilder::build).collect(Collectors.toList()));
    }

    public static SlashCommandBuilder with(String name, String description, List<SlashCommandOption> options) {
        return SlashCommand.with(name, description).setOptions(options);
    }

    public static SlashCommandBuilder withRequiredPermissions(String name, String description, PermissionType ... requiredPermissions) {
        return (SlashCommandBuilder)((SlashCommandBuilder)((SlashCommandBuilder)new SlashCommandBuilder().setName(name)).setDefaultEnabledForPermissions(requiredPermissions)).setDescription(description);
    }

    public static SlashCommandBuilder withRequiredPermissions(String name, String description, EnumSet<PermissionType> requiredPermissions) {
        return (SlashCommandBuilder)((SlashCommandBuilder)((SlashCommandBuilder)new SlashCommandBuilder().setName(name)).setDefaultEnabledForPermissions(requiredPermissions)).setDescription(description);
    }

    public static SlashCommandBuilder withRequiredPermissions(String name, String description, List<SlashCommandOption> options, PermissionType ... requiredPermissions) {
        return (SlashCommandBuilder)SlashCommand.with(name, description).setOptions(options).setDefaultEnabledForPermissions(requiredPermissions);
    }

    public static SlashCommandBuilder withRequiredPermissions(String name, String description, List<SlashCommandOption> options, EnumSet<PermissionType> requiredPermissions) {
        return (SlashCommandBuilder)SlashCommand.with(name, description).setOptions(options).setDefaultEnabledForPermissions(requiredPermissions);
    }

    public static SlashCommandBuilder createPrefilledSlashCommandBuilder(SlashCommand slashCommand) {
        return SlashCommand.with(slashCommand.getName(), slashCommand.getDescription()).setOptions(slashCommand.getOptions());
    }

    default public SlashCommandUpdater createSlashCommandUpdater() {
        return new SlashCommandUpdater(this.getId());
    }
}

