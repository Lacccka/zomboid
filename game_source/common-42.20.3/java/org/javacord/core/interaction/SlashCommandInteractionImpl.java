/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.interaction;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.javacord.api.entity.Attachment;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.InteractionType;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandInteractionOption;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.entity.AttachmentImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.entity.user.MemberImpl;
import org.javacord.core.entity.user.UserImpl;
import org.javacord.core.interaction.ApplicationCommandInteractionImpl;
import org.javacord.core.interaction.SlashCommandInteractionOptionImpl;

public class SlashCommandInteractionImpl
extends ApplicationCommandInteractionImpl
implements SlashCommandInteraction {
    private final List<SlashCommandInteractionOption> options;

    public SlashCommandInteractionImpl(DiscordApiImpl api, TextChannel channel, JsonNode jsonData) {
        super(api, channel, jsonData);
        JsonNode data = jsonData.get("data");
        HashMap<Long, User> resolvedUsers = new HashMap<Long, User>();
        HashMap<Long, Attachment> resolvedAttachments = new HashMap<Long, Attachment>();
        if (data.has("resolved")) {
            JsonNode resolved = data.get("resolved");
            if (jsonData.has("guild_id")) {
                ServerImpl server = (ServerImpl)api.getServerById(jsonData.get("guild_id").asLong()).orElseThrow(AssertionError::new);
                if (resolved.has("members")) {
                    resolved.get("members").fields().forEachRemaining(memberNode -> {
                        Long id = Long.parseLong((String)memberNode.getKey());
                        JsonNode userData = resolved.get("users").get(String.valueOf(id));
                        resolvedUsers.put(id, new UserImpl(api, userData, (JsonNode)memberNode.getValue(), server));
                    });
                }
            }
            if (resolved.has("users")) {
                resolved.get("users").fields().forEachRemaining(userNode -> {
                    JsonNode userData = (JsonNode)userNode.getValue();
                    Long id = Long.parseLong((String)userNode.getKey());
                    if (!resolvedUsers.containsKey(id)) {
                        resolvedUsers.put(id, new UserImpl(api, userData, (MemberImpl)null, null));
                    }
                });
            }
            if (resolved.has("attachments")) {
                resolved.get("attachments").fields().forEachRemaining(attachmentNode -> resolvedAttachments.put(Long.parseLong((String)attachmentNode.getKey()), new AttachmentImpl(api, (JsonNode)attachmentNode.getValue())));
            }
        }
        this.options = new ArrayList<SlashCommandInteractionOption>();
        if (data.has("options") && data.get("options").isArray()) {
            for (JsonNode optionJson : data.get("options")) {
                this.options.add(new SlashCommandInteractionOptionImpl(api, optionJson, resolvedUsers, resolvedAttachments));
            }
        }
    }

    @Override
    public InteractionType getType() {
        return InteractionType.APPLICATION_COMMAND;
    }

    @Override
    public List<SlashCommandInteractionOption> getOptions() {
        return Collections.unmodifiableList(this.options);
    }

    @Override
    public long getCommandId() {
        return this.commandId;
    }

    @Override
    public String getCommandIdAsString() {
        return String.valueOf(this.commandId);
    }

    @Override
    public String getCommandName() {
        return this.commandName;
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

    @Override
    public String getFullCommandName() {
        return this.getCommandName() + this.getNestedCommandNamesRecursive(this.getOptions());
    }

    private String getNestedCommandNamesRecursive(List<SlashCommandInteractionOption> options) {
        if (!options.isEmpty() && options.get(0).isSubcommandOrGroup()) {
            return " " + options.get(0).getName() + this.getNestedCommandNamesRecursive(options.get(0).getOptions());
        }
        return "";
    }
}

