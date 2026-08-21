/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.message.component;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.message.component.ComponentType;
import org.javacord.api.entity.message.component.LowLevelComponent;
import org.javacord.api.entity.message.component.SelectMenuBuilder;
import org.javacord.api.entity.message.component.SelectMenuOption;

public interface SelectMenu
extends LowLevelComponent {
    public EnumSet<ChannelType> getChannelTypes();

    public Optional<String> getPlaceholder();

    public String getCustomId();

    public int getMinimumValues();

    public int getMaximumValues();

    public List<SelectMenuOption> getOptions();

    public boolean isDisabled();

    @Deprecated
    public static SelectMenu create(String customId, List<SelectMenuOption> options) {
        return new SelectMenuBuilder(ComponentType.SELECT_MENU_STRING, customId).addOptions(options).build();
    }

    @Deprecated
    public static SelectMenu create(String customId, List<SelectMenuOption> options, boolean isDisabled) {
        return new SelectMenuBuilder(ComponentType.SELECT_MENU_STRING, customId).addOptions(options).setDisabled(isDisabled).build();
    }

    @Deprecated
    public static SelectMenu create(String customId, String placeholder, List<SelectMenuOption> options) {
        return new SelectMenuBuilder(ComponentType.SELECT_MENU_STRING, customId).setPlaceholder(placeholder).addOptions(options).build();
    }

    @Deprecated
    public static SelectMenu create(String customId, String placeholder, List<SelectMenuOption> options, boolean isDisabled) {
        return new SelectMenuBuilder(ComponentType.SELECT_MENU_STRING, customId).setPlaceholder(placeholder).addOptions(options).setDisabled(isDisabled).build();
    }

    @Deprecated
    public static SelectMenu create(String customId, String placeholder, int minimumValues, int maximumValues, List<SelectMenuOption> options) {
        return new SelectMenuBuilder(ComponentType.SELECT_MENU_STRING, customId).setPlaceholder(placeholder).setMinimumValues(minimumValues).setMaximumValues(maximumValues).addOptions(options).build();
    }

    @Deprecated
    public static SelectMenu create(String customId, String placeholder, int minimumValues, int maximumValues, List<SelectMenuOption> options, boolean isDisabled) {
        return new SelectMenuBuilder(ComponentType.SELECT_MENU_STRING, customId).setPlaceholder(placeholder).setMinimumValues(minimumValues).setMaximumValues(maximumValues).addOptions(options).setDisabled(isDisabled).build();
    }

    public static SelectMenu createStringMenu(String customId, List<SelectMenuOption> options) {
        return new SelectMenuBuilder(ComponentType.SELECT_MENU_STRING, customId).addOptions(options).build();
    }

    public static SelectMenu createStringMenu(String customId, List<SelectMenuOption> options, boolean isDisabled) {
        return new SelectMenuBuilder(ComponentType.SELECT_MENU_STRING, customId).addOptions(options).setDisabled(isDisabled).build();
    }

    public static SelectMenu createStringMenu(String customId, String placeholder, List<SelectMenuOption> options) {
        return new SelectMenuBuilder(ComponentType.SELECT_MENU_STRING, customId).setPlaceholder(placeholder).addOptions(options).build();
    }

    public static SelectMenu createStringMenu(String customId, String placeholder, List<SelectMenuOption> options, boolean isDisabled) {
        return new SelectMenuBuilder(ComponentType.SELECT_MENU_STRING, customId).setPlaceholder(placeholder).addOptions(options).setDisabled(isDisabled).build();
    }

    public static SelectMenu createStringMenu(String customId, String placeholder, int minimumValues, int maximumValues, List<SelectMenuOption> options) {
        return new SelectMenuBuilder(ComponentType.SELECT_MENU_STRING, customId).setPlaceholder(placeholder).setMinimumValues(minimumValues).setMaximumValues(maximumValues).addOptions(options).build();
    }

    public static SelectMenu createStringMenu(String customId, String placeholder, int minimumValues, int maximumValues, List<SelectMenuOption> options, boolean isDisabled) {
        return new SelectMenuBuilder(ComponentType.SELECT_MENU_STRING, customId).setPlaceholder(placeholder).setMinimumValues(minimumValues).setMaximumValues(maximumValues).addOptions(options).setDisabled(isDisabled).build();
    }

    public static SelectMenu createChannelMenu(String customId) {
        return new SelectMenuBuilder(ComponentType.SELECT_MENU_CHANNEL, customId).build();
    }

    public static SelectMenu createChannelMenu(String customId, Iterable<ChannelType> channelTypes) {
        return new SelectMenuBuilder(ComponentType.SELECT_MENU_CHANNEL, customId).addChannelTypes(channelTypes).build();
    }

    public static SelectMenu createChannelMenu(String customId, Iterable<ChannelType> channelTypes, boolean isDisabled) {
        return new SelectMenuBuilder(ComponentType.SELECT_MENU_CHANNEL, customId).addChannelTypes(channelTypes).setDisabled(isDisabled).build();
    }

    public static SelectMenu createChannelMenu(String customId, String placeholder, Iterable<ChannelType> channelTypes) {
        return new SelectMenuBuilder(ComponentType.SELECT_MENU_CHANNEL, customId).setPlaceholder(placeholder).addChannelTypes(channelTypes).build();
    }

    public static SelectMenu createChannelMenu(String customId, String placeholder, Iterable<ChannelType> channelTypes, boolean isDisabled) {
        return new SelectMenuBuilder(ComponentType.SELECT_MENU_CHANNEL, customId).setPlaceholder(placeholder).addChannelTypes(channelTypes).setDisabled(isDisabled).build();
    }

    public static SelectMenu createChannelMenu(String customId, String placeholder, int minimumValues, int maximumValues, Iterable<ChannelType> channelTypes) {
        return new SelectMenuBuilder(ComponentType.SELECT_MENU_CHANNEL, customId).setPlaceholder(placeholder).setMinimumValues(minimumValues).setMaximumValues(maximumValues).addChannelTypes(channelTypes).build();
    }

    public static SelectMenu createChannelMenu(String customId, String placeholder, int minimumValues, int maximumValues, Iterable<ChannelType> channelTypes, boolean isDisabled) {
        return new SelectMenuBuilder(ComponentType.SELECT_MENU_CHANNEL, customId).setPlaceholder(placeholder).setMinimumValues(minimumValues).setMaximumValues(maximumValues).addChannelTypes(channelTypes).setDisabled(isDisabled).build();
    }

    public static SelectMenu createRoleMenu(String customId) {
        return new SelectMenuBuilder(ComponentType.SELECT_MENU_ROLE, customId).build();
    }

    public static SelectMenu createRoleMenu(String customId, boolean isDisabled) {
        return new SelectMenuBuilder(ComponentType.SELECT_MENU_ROLE, customId).setDisabled(isDisabled).build();
    }

    public static SelectMenu createRoleMenu(String customId, String placeholder) {
        return new SelectMenuBuilder(ComponentType.SELECT_MENU_ROLE, customId).setPlaceholder(placeholder).build();
    }

    public static SelectMenu createRoleMenu(String customId, String placeholder, boolean isDisabled) {
        return new SelectMenuBuilder(ComponentType.SELECT_MENU_ROLE, customId).setPlaceholder(placeholder).setDisabled(isDisabled).build();
    }

    public static SelectMenu createRoleMenu(String customId, String placeholder, int minimumValues, int maximumValues) {
        return new SelectMenuBuilder(ComponentType.SELECT_MENU_ROLE, customId).setPlaceholder(placeholder).setMinimumValues(minimumValues).setMaximumValues(maximumValues).build();
    }

    public static SelectMenu createRoleMenu(String customId, String placeholder, int minimumValues, int maximumValues, boolean isDisabled) {
        return new SelectMenuBuilder(ComponentType.SELECT_MENU_ROLE, customId).setPlaceholder(placeholder).setMinimumValues(minimumValues).setMaximumValues(maximumValues).setDisabled(isDisabled).build();
    }

    public static SelectMenu createMentionableMenu(String customId) {
        return new SelectMenuBuilder(ComponentType.SELECT_MENU_MENTIONABLE, customId).build();
    }

    public static SelectMenu createMentionableMenu(String customId, boolean isDisabled) {
        return new SelectMenuBuilder(ComponentType.SELECT_MENU_MENTIONABLE, customId).setDisabled(isDisabled).build();
    }

    public static SelectMenu createMentionableMenu(String customId, String placeholder) {
        return new SelectMenuBuilder(ComponentType.SELECT_MENU_MENTIONABLE, customId).setPlaceholder(placeholder).build();
    }

    public static SelectMenu createMentionableMenu(String customId, String placeholder, boolean isDisabled) {
        return new SelectMenuBuilder(ComponentType.SELECT_MENU_MENTIONABLE, customId).setPlaceholder(placeholder).setDisabled(isDisabled).build();
    }

    public static SelectMenu createMentionableMenu(String customId, String placeholder, int minimumValues, int maximumValues) {
        return new SelectMenuBuilder(ComponentType.SELECT_MENU_MENTIONABLE, customId).setPlaceholder(placeholder).setMinimumValues(minimumValues).setMaximumValues(maximumValues).build();
    }

    public static SelectMenu createMentionableMenu(String customId, String placeholder, int minimumValues, int maximumValues, boolean isDisabled) {
        return new SelectMenuBuilder(ComponentType.SELECT_MENU_MENTIONABLE, customId).setPlaceholder(placeholder).setMinimumValues(minimumValues).setMaximumValues(maximumValues).setDisabled(isDisabled).build();
    }

    public static SelectMenu createUserMenu(String customId) {
        return new SelectMenuBuilder(ComponentType.SELECT_MENU_USER, customId).build();
    }

    public static SelectMenu createUserMenu(String customId, boolean isDisabled) {
        return new SelectMenuBuilder(ComponentType.SELECT_MENU_USER, customId).setDisabled(isDisabled).build();
    }

    public static SelectMenu createUserMenu(String customId, String placeholder) {
        return new SelectMenuBuilder(ComponentType.SELECT_MENU_USER, customId).setPlaceholder(placeholder).build();
    }

    public static SelectMenu createUserMenu(String customId, String placeholder, boolean isDisabled) {
        return new SelectMenuBuilder(ComponentType.SELECT_MENU_USER, customId).setPlaceholder(placeholder).setDisabled(isDisabled).build();
    }

    public static SelectMenu createUserMenu(String customId, String placeholder, int minimumValues, int maximumValues) {
        return new SelectMenuBuilder(ComponentType.SELECT_MENU_USER, customId).setPlaceholder(placeholder).setMinimumValues(minimumValues).setMaximumValues(maximumValues).build();
    }

    public static SelectMenu createUserMenu(String customId, String placeholder, int minimumValues, int maximumValues, boolean isDisabled) {
        return new SelectMenuBuilder(ComponentType.SELECT_MENU_USER, customId).setPlaceholder(placeholder).setMinimumValues(minimumValues).setMaximumValues(maximumValues).setDisabled(isDisabled).build();
    }
}

