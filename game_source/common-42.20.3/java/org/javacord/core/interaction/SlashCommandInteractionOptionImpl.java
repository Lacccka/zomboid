/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.interaction;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.Attachment;
import org.javacord.api.entity.Mentionable;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.SlashCommandInteractionOption;
import org.javacord.api.interaction.SlashCommandOptionType;
import org.javacord.core.util.logging.LoggerUtil;

public class SlashCommandInteractionOptionImpl
implements SlashCommandInteractionOption {
    private final DiscordApi api;
    private final Map<Long, User> resolvedUsers;
    private final String name;
    private final String stringRepresentation;
    private final String stringValue;
    private final Long longValue;
    private final Boolean booleanValue;
    private final Long userValue;
    private final Long channelValue;
    private final Long roleValue;
    private final Long mentionableValue;
    private final Attachment attachmentValue;
    private final Double decimalValue;
    private final List<SlashCommandInteractionOption> options;
    private final Boolean focused;
    private static final Logger LOGGER = LoggerUtil.getLogger(SlashCommandInteractionOptionImpl.class);

    public SlashCommandInteractionOptionImpl(DiscordApi api, JsonNode jsonData, Map<Long, User> resolvedUsers, Map<Long, Attachment> resolvedAttachments) {
        this.api = api;
        this.resolvedUsers = resolvedUsers;
        this.name = jsonData.get("name").asText();
        this.focused = jsonData.has("focused") ? Boolean.valueOf(jsonData.get("focused").asBoolean()) : null;
        this.options = new ArrayList<SlashCommandInteractionOption>();
        JsonNode valueNode = jsonData.get("value");
        String localStringRepresentation = null;
        String localStringValue = null;
        Long localLongValue = null;
        Boolean localBooleanValue = null;
        Long localUserValue = null;
        Long localChannelValue = null;
        Long localRoleValue = null;
        Long localMentionableValue = null;
        Double localDecimalValue = null;
        Attachment localAttachmentValue = null;
        int typeInt = jsonData.get("type").asInt();
        SlashCommandOptionType slashCommandOptionType = SlashCommandOptionType.fromValue(typeInt);
        switch (slashCommandOptionType) {
            case SUB_COMMAND: 
            case SUB_COMMAND_GROUP: {
                if (!jsonData.has("options") || !jsonData.get("options").isArray()) break;
                for (JsonNode optionJson : jsonData.get("options")) {
                    this.options.add(new SlashCommandInteractionOptionImpl(api, optionJson, resolvedUsers, resolvedAttachments));
                }
                break;
            }
            case STRING: {
                localStringRepresentation = localStringValue = valueNode.asText();
                break;
            }
            case LONG: {
                localLongValue = valueNode.asLong();
                localStringRepresentation = String.valueOf(localLongValue);
                break;
            }
            case BOOLEAN: {
                localBooleanValue = valueNode.asBoolean();
                localStringRepresentation = String.valueOf(localBooleanValue);
                break;
            }
            case USER: {
                localUserValue = Long.parseLong(valueNode.asText());
                localStringRepresentation = String.valueOf(localUserValue);
                break;
            }
            case CHANNEL: {
                localChannelValue = Long.parseLong(valueNode.asText());
                localStringRepresentation = String.valueOf(localChannelValue);
                break;
            }
            case ROLE: {
                localRoleValue = Long.parseLong(valueNode.asText());
                localStringRepresentation = String.valueOf(localRoleValue);
                break;
            }
            case MENTIONABLE: {
                localMentionableValue = Long.parseLong(valueNode.asText());
                localStringRepresentation = String.valueOf(localMentionableValue);
                break;
            }
            case DECIMAL: {
                localDecimalValue = valueNode.asDouble();
                localStringRepresentation = String.valueOf(localDecimalValue);
                break;
            }
            case ATTACHMENT: {
                localAttachmentValue = resolvedAttachments.get(Long.parseLong(valueNode.asText()));
                localStringRepresentation = valueNode.asText();
                break;
            }
            default: {
                LOGGER.warn("Received slash command option of unknown type <{}>. Please contact the developer!", (Object)typeInt);
            }
        }
        this.stringRepresentation = localStringRepresentation;
        this.stringValue = localStringValue;
        this.longValue = localLongValue;
        this.booleanValue = localBooleanValue;
        this.userValue = localUserValue;
        this.channelValue = localChannelValue;
        this.roleValue = localRoleValue;
        this.mentionableValue = localMentionableValue;
        this.decimalValue = localDecimalValue;
        this.attachmentValue = localAttachmentValue;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Optional<Boolean> isFocused() {
        return Optional.ofNullable(this.focused);
    }

    @Override
    public Optional<String> getStringRepresentationValue() {
        return Optional.ofNullable(this.stringRepresentation);
    }

    @Override
    public Optional<String> getStringValue() {
        return Optional.ofNullable(this.stringValue);
    }

    @Override
    public Optional<Long> getLongValue() {
        return Optional.ofNullable(this.longValue);
    }

    @Override
    public Optional<Boolean> getBooleanValue() {
        return Optional.ofNullable(this.booleanValue);
    }

    @Override
    public Optional<User> getUserValue() {
        return Optional.ofNullable(this.userValue).map(id -> this.api.getCachedUserById((long)id).orElseGet(() -> this.resolvedUsers.get(id)));
    }

    @Override
    public Optional<CompletableFuture<User>> requestUserValue() {
        return Optional.ofNullable(this.userValue).map(this.api::getUserById);
    }

    @Override
    public Optional<ServerChannel> getChannelValue() {
        return Optional.ofNullable(this.channelValue).flatMap(this.api::getServerChannelById);
    }

    @Override
    public Optional<Attachment> getAttachmentValue() {
        return Optional.ofNullable(this.attachmentValue);
    }

    @Override
    public Optional<Role> getRoleValue() {
        return Optional.ofNullable(this.roleValue).flatMap(this.api::getRoleById);
    }

    @Override
    public Optional<Mentionable> getMentionableValue() {
        Optional<Mentionable> mentionable = Optional.empty();
        if (this.mentionableValue != null) {
            mentionable = this.api.getRoleById(this.mentionableValue).map(Mentionable.class::cast);
            if (mentionable.isPresent()) {
                return mentionable;
            }
            mentionable = this.api.getServerChannelById(this.mentionableValue).map(Mentionable.class::cast);
            if (mentionable.isPresent()) {
                return mentionable;
            }
            mentionable = this.api.getCachedUserById(this.mentionableValue).map(Mentionable.class::cast);
            if (!mentionable.isPresent()) {
                mentionable = Optional.ofNullable(this.resolvedUsers.get(this.mentionableValue)).map(Mentionable.class::cast);
            }
        }
        return mentionable;
    }

    @Override
    public Optional<Double> getDecimalValue() {
        return Optional.ofNullable(this.decimalValue);
    }

    @Override
    public Optional<CompletableFuture<Mentionable>> requestMentionableValue() {
        Optional<CompletableFuture<Mentionable>> cacheOptional = this.getMentionableValue().map(CompletableFuture::completedFuture);
        if (cacheOptional.isPresent()) {
            return cacheOptional;
        }
        return Optional.ofNullable(this.mentionableValue).map(this.api::getUserById).map(future -> future.thenApply(Mentionable.class::cast));
    }

    @Override
    public List<SlashCommandInteractionOption> getOptions() {
        return Collections.unmodifiableList(this.options);
    }

    @Override
    public List<SlashCommandInteractionOption> getArguments() {
        return this.getArgumentsRecursive(this.getOptions());
    }

    private List<SlashCommandInteractionOption> getArgumentsRecursive(List<SlashCommandInteractionOption> options) {
        if (options.isEmpty()) {
            return Collections.emptyList();
        }
        if (options.get(0).isSubcommandOrGroup()) {
            return this.getArgumentsRecursive(options.get(0).getOptions());
        }
        return options;
    }
}

