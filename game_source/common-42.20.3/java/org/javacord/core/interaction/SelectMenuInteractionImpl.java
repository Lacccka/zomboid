/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.interaction;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.Mentionable;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.component.ComponentType;
import org.javacord.api.entity.message.component.SelectMenuOption;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.SelectMenuInteraction;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.entity.message.component.SelectMenuOptionImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.entity.user.MemberImpl;
import org.javacord.core.entity.user.UserImpl;
import org.javacord.core.interaction.MessageComponentInteractionImpl;
import org.javacord.core.util.logging.LoggerUtil;

public class SelectMenuInteractionImpl
extends MessageComponentInteractionImpl
implements SelectMenuInteraction {
    private static final Logger logger = LoggerUtil.getLogger(SelectMenuInteractionImpl.class);
    private final List<SelectMenuOption> selectMenuOptions = new ArrayList<SelectMenuOption>();
    private final List<SelectMenuOption> chosenSelectMenuOption = new ArrayList<SelectMenuOption>();
    private final List<ServerChannel> selectMenuChannels = new ArrayList<ServerChannel>();
    private final List<User> selectMenuUsers = new ArrayList<User>();
    private final List<Role> selectMenuRoles = new ArrayList<Role>();
    private final List<Mentionable> selectMenuMentionables = new ArrayList<Mentionable>();
    private String placeholder;
    private int minimumValues;
    private int maximumValues;
    private final ComponentType componentType;

    public SelectMenuInteractionImpl(DiscordApiImpl api, TextChannel channel, JsonNode jsonData) {
        super(api, channel, jsonData);
        this.componentType = ComponentType.fromId(jsonData.get("data").get("component_type").asInt());
        JsonNode messageObject = jsonData.get("message");
        JsonNode componentsObject = messageObject.get("components");
        JsonNode dataObject = jsonData.get("data");
        block7: for (JsonNode highLevelComponent : componentsObject) {
            for (JsonNode lowLevelComponent : highLevelComponent.get("components")) {
                if (!this.componentType.isSelectMenuType() || !lowLevelComponent.has("custom_id") || !lowLevelComponent.get("custom_id").asText().equals(dataObject.get("custom_id").asText())) continue;
                this.placeholder = lowLevelComponent.has("placeholder") ? lowLevelComponent.get("placeholder").asText() : null;
                this.maximumValues = lowLevelComponent.has("max_values") ? lowLevelComponent.get("max_values").asInt() : 1;
                int n = this.minimumValues = lowLevelComponent.has("min_values") ? lowLevelComponent.get("min_values").asInt() : 1;
                if (!lowLevelComponent.hasNonNull("options")) break block7;
                for (JsonNode optionObject : lowLevelComponent.get("options")) {
                    this.selectMenuOptions.add(new SelectMenuOptionImpl(optionObject));
                }
                break block7;
            }
        }
        JsonNode valuesArray = dataObject.get("values");
        switch (this.componentType) {
            case SELECT_MENU_CHANNEL: {
                this.selectMenuChannels.addAll(this.getSelectMenuChannels(valuesArray));
                break;
            }
            case SELECT_MENU_ROLE: {
                this.selectMenuRoles.addAll(this.getSelectMenuRoles(valuesArray));
                break;
            }
            case SELECT_MENU_USER: {
                this.selectMenuUsers.addAll(this.getSelectMenuUsers(dataObject));
                break;
            }
            case SELECT_MENU_MENTIONABLE: {
                this.selectMenuMentionables.addAll(this.getSelectMenuRoles(valuesArray));
                this.selectMenuMentionables.addAll(this.getSelectMenuUsers(dataObject));
                break;
            }
            case SELECT_MENU_STRING: {
                for (JsonNode jsonNode : valuesArray) {
                    this.chosenSelectMenuOption.addAll(this.selectMenuOptions.stream().filter(option -> option.getValue().equals(jsonNode.asText())).collect(Collectors.toList()));
                }
                break;
            }
            default: {
                logger.warn("Creating a SelectMenuInteractionImpl with an unhandled Select Menu component type which is most likely not wanted!");
            }
        }
    }

    private List<User> getSelectMenuUsers(JsonNode dataObject) {
        ArrayList<User> usersList = new ArrayList<User>();
        JsonNode valuesArray = dataObject.get("values");
        JsonNode resolved = dataObject.get("resolved");
        JsonNode users = resolved.get("users");
        JsonNode members = resolved.get("members");
        for (JsonNode value : valuesArray) {
            long id = value.asLong();
            Optional optionalMember = this.getServer().flatMap(server -> server.getMemberById(id));
            Optional<User> optionalUser = this.getApi().getCachedUserById(id);
            if (optionalMember.isPresent()) {
                usersList.add((User)optionalMember.orElseThrow(AssertionError::new));
                continue;
            }
            if (optionalUser.isPresent()) {
                usersList.add(optionalUser.orElseThrow(AssertionError::new));
                continue;
            }
            if (members.has(value.asText())) {
                this.getServer().ifPresent(server -> usersList.add(new UserImpl((DiscordApiImpl)this.getApi(), users.get(value.asText()), members.get(value.asText()), (ServerImpl)server)));
                continue;
            }
            if (!users.has(value.asText())) continue;
            usersList.add(new UserImpl((DiscordApiImpl)this.getApi(), users.get(value.asText()), (MemberImpl)null, null));
        }
        return usersList;
    }

    private List<ServerChannel> getSelectMenuChannels(JsonNode valuesArray) {
        ArrayList<ServerChannel> channels = new ArrayList<ServerChannel>();
        for (JsonNode jsonNode : valuesArray) {
            this.getServer().flatMap(server -> server.getChannelById(jsonNode.asLong())).ifPresent(channels::add);
        }
        return channels;
    }

    private List<Role> getSelectMenuRoles(JsonNode valuesArray) {
        ArrayList<Role> roles = new ArrayList<Role>();
        for (JsonNode jsonNode : valuesArray) {
            this.getServer().flatMap(server -> server.getRoleById(jsonNode.asLong())).ifPresent(roles::add);
        }
        return roles;
    }

    @Override
    public ComponentType getComponentType() {
        return this.componentType;
    }

    @Override
    public List<Role> getSelectedRoles() {
        return Collections.unmodifiableList(this.selectMenuRoles);
    }

    @Override
    public List<User> getSelectedUsers() {
        return Collections.unmodifiableList(this.selectMenuUsers);
    }

    @Override
    public List<ServerChannel> getSelectedChannels() {
        return Collections.unmodifiableList(this.selectMenuChannels);
    }

    @Override
    public List<Mentionable> getSelectedMentionables() {
        return Collections.unmodifiableList(this.selectMenuMentionables);
    }

    @Override
    public List<SelectMenuOption> getChosenOptions() {
        return this.chosenSelectMenuOption;
    }

    @Override
    public List<SelectMenuOption> getPossibleOptions() {
        return this.selectMenuOptions;
    }

    @Override
    public Optional<String> getPlaceholder() {
        return Optional.ofNullable(this.placeholder);
    }

    @Override
    public int getMinimumValues() {
        return this.minimumValues;
    }

    @Override
    public int getMaximumValues() {
        return this.maximumValues;
    }
}

