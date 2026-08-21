/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.handler.interaction;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.component.ComponentType;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.interaction.AutocompleteCreateEvent;
import org.javacord.api.event.interaction.ButtonClickEvent;
import org.javacord.api.event.interaction.InteractionCreateEvent;
import org.javacord.api.event.interaction.MessageComponentCreateEvent;
import org.javacord.api.event.interaction.MessageContextMenuCommandEvent;
import org.javacord.api.event.interaction.ModalSubmitEvent;
import org.javacord.api.event.interaction.SelectMenuChooseEvent;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.event.interaction.UserContextMenuCommandEvent;
import org.javacord.api.interaction.ApplicationCommandType;
import org.javacord.api.interaction.InteractionType;
import org.javacord.core.entity.channel.PrivateChannelImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.entity.user.MemberImpl;
import org.javacord.core.entity.user.UserImpl;
import org.javacord.core.event.interaction.AutocompleteCreateEventImpl;
import org.javacord.core.event.interaction.ButtonClickEventImpl;
import org.javacord.core.event.interaction.InteractionCreateEventImpl;
import org.javacord.core.event.interaction.MessageComponentCreateEventImpl;
import org.javacord.core.event.interaction.MessageContextMenuCommandEventImpl;
import org.javacord.core.event.interaction.ModalSubmitEventImpl;
import org.javacord.core.event.interaction.SelectMenuChooseEventImpl;
import org.javacord.core.event.interaction.SlashCommandCreateEventImpl;
import org.javacord.core.event.interaction.UserContextMenuCommandEventImpl;
import org.javacord.core.interaction.AutocompleteInteractionImpl;
import org.javacord.core.interaction.ButtonInteractionImpl;
import org.javacord.core.interaction.InteractionImpl;
import org.javacord.core.interaction.MessageContextMenuInteractionImpl;
import org.javacord.core.interaction.ModalInteractionImpl;
import org.javacord.core.interaction.SelectMenuInteractionImpl;
import org.javacord.core.interaction.SlashCommandInteractionImpl;
import org.javacord.core.interaction.UserContextMenuInteractionImpl;
import org.javacord.core.util.event.DispatchQueueSelector;
import org.javacord.core.util.gateway.PacketHandler;
import org.javacord.core.util.logging.LoggerUtil;

public class InteractionCreateHandler
extends PacketHandler {
    private static final Logger logger = LoggerUtil.getLogger(InteractionCreateHandler.class);

    public InteractionCreateHandler(DiscordApi api) {
        super(api, true, "INTERACTION_CREATE");
    }

    @Override
    public void handle(JsonNode packet) {
        InteractionImpl interaction;
        TextChannel channel = null;
        if (packet.hasNonNull("channel_id")) {
            long channelId = packet.get("channel_id").asLong();
            if (packet.hasNonNull("guild_id")) {
                channel = this.api.getTextChannelById(channelId).orElse(null);
            } else {
                UserImpl user = new UserImpl(this.api, packet.get("user"), (MemberImpl)null, null);
                channel = PrivateChannelImpl.getOrCreatePrivateChannel(this.api, channelId, user.getId(), user);
            }
        }
        int typeId = packet.get("type").asInt();
        InteractionType interactionType = InteractionType.fromValue(typeId);
        ComponentType componentType = null;
        block0 : switch (interactionType) {
            case APPLICATION_COMMAND: {
                int applicationCommandTypeId = packet.get("data").get("type").asInt();
                ApplicationCommandType applicationCommandType = ApplicationCommandType.fromValue(applicationCommandTypeId);
                switch (applicationCommandType) {
                    case SLASH: {
                        interaction = new SlashCommandInteractionImpl(this.api, channel, packet);
                        break block0;
                    }
                    case USER: {
                        interaction = new UserContextMenuInteractionImpl(this.api, channel, packet);
                        break block0;
                    }
                    case MESSAGE: {
                        interaction = new MessageContextMenuInteractionImpl(this.api, channel, packet);
                        break block0;
                    }
                }
                logger.info("Got application command interaction of unknown type <{}>. Please contact the developer!", (Object)applicationCommandTypeId);
                return;
            }
            case MESSAGE_COMPONENT: {
                int componentTypeId = packet.get("data").get("component_type").asInt();
                componentType = ComponentType.fromId(componentTypeId);
                if (componentType == ComponentType.BUTTON) {
                    interaction = new ButtonInteractionImpl(this.api, channel, packet);
                    break;
                }
                if (componentType.isSelectMenuType()) {
                    interaction = new SelectMenuInteractionImpl(this.api, channel, packet);
                    break;
                }
                logger.warn("Received message component interaction of unknown type <{}>. Please contact the developer!", (Object)componentTypeId);
                return;
            }
            case APPLICATION_COMMAND_AUTOCOMPLETE: {
                interaction = new AutocompleteInteractionImpl(this.api, channel, packet);
                break;
            }
            case MODAL_SUBMIT: {
                interaction = new ModalInteractionImpl(this.api, channel, packet);
                break;
            }
            default: {
                logger.warn("Received interaction of unknown type <{}>. Please contact the developer!", (Object)typeId);
                return;
            }
        }
        InteractionCreateEventImpl event = new InteractionCreateEventImpl(interaction);
        ServerImpl server = interaction.getServer().orElse(null);
        this.api.getEventDispatcher().dispatchInteractionCreateEvent(server == null ? this.api : server, (Server)server, (TextChannel)interaction.getChannel().orElse(null), interaction.getUser(), (InteractionCreateEvent)event);
        block11 : switch (interactionType) {
            case APPLICATION_COMMAND: {
                int applicationCommandTypeId = packet.get("data").get("type").asInt();
                ApplicationCommandType applicationCommandType = ApplicationCommandType.fromValue(applicationCommandTypeId);
                switch (applicationCommandType) {
                    case SLASH: {
                        SlashCommandCreateEventImpl slashCommandCreateEvent = new SlashCommandCreateEventImpl(interaction);
                        this.api.getEventDispatcher().dispatchSlashCommandCreateEvent(server == null ? this.api : server, (Server)server, (TextChannel)interaction.getChannel().orElse(null), interaction.getUser(), (SlashCommandCreateEvent)slashCommandCreateEvent);
                        break block11;
                    }
                    case USER: {
                        UserContextMenuCommandEventImpl userContextMenuCommandEvent = new UserContextMenuCommandEventImpl(interaction);
                        this.api.getEventDispatcher().dispatchUserContextMenuCommandEvent((DispatchQueueSelector)server, (Server)server, (TextChannel)interaction.getChannel().orElse(null), interaction.getUser(), (UserContextMenuCommandEvent)userContextMenuCommandEvent);
                        break block11;
                    }
                    case MESSAGE: {
                        MessageContextMenuCommandEventImpl messageContextMenuCommandEvent = new MessageContextMenuCommandEventImpl(interaction);
                        this.api.getEventDispatcher().dispatchMessageContextMenuCommandEvent((DispatchQueueSelector)server, interaction.asMessageContextMenuInteraction().orElseThrow(AssertionError::new).getTarget().getId(), (Server)server, (TextChannel)interaction.getChannel().orElse(null), interaction.getUser(), (MessageContextMenuCommandEvent)messageContextMenuCommandEvent);
                        break block11;
                    }
                }
                logger.info("Got application command interaction of unknown type <{}>. Please contact the developer!", (Object)applicationCommandTypeId);
                return;
            }
            case MESSAGE_COMPONENT: {
                MessageComponentCreateEventImpl messageComponentCreateEvent = new MessageComponentCreateEventImpl(interaction);
                long messageId = messageComponentCreateEvent.getMessageComponentInteraction().getMessage().getId();
                this.api.getEventDispatcher().dispatchMessageComponentCreateEvent(server == null ? this.api : server, messageId, (Server)server, (TextChannel)interaction.getChannel().orElse(null), interaction.getUser(), (MessageComponentCreateEvent)messageComponentCreateEvent);
                if (componentType == ComponentType.BUTTON) {
                    ButtonClickEventImpl buttonClickEvent = new ButtonClickEventImpl(interaction);
                    this.api.getEventDispatcher().dispatchButtonClickEvent(server == null ? this.api : server, messageId, (Server)server, (TextChannel)interaction.getChannel().orElse(null), interaction.getUser(), (ButtonClickEvent)buttonClickEvent);
                    break;
                }
                if (!componentType.isSelectMenuType()) break;
                SelectMenuChooseEventImpl selectMenuChooseEvent = new SelectMenuChooseEventImpl(interaction);
                this.api.getEventDispatcher().dispatchSelectMenuChooseEvent(server == null ? this.api : server, messageId, (Server)server, (TextChannel)interaction.getChannel().orElse(null), interaction.getUser(), (SelectMenuChooseEvent)selectMenuChooseEvent);
                break;
            }
            case APPLICATION_COMMAND_AUTOCOMPLETE: {
                AutocompleteCreateEventImpl autocompleteCreateEvent = new AutocompleteCreateEventImpl(interaction);
                this.api.getEventDispatcher().dispatchAutocompleteCreateEvent(server == null ? this.api : server, (Server)server, (TextChannel)interaction.getChannel().orElse(null), interaction.getUser(), (AutocompleteCreateEvent)autocompleteCreateEvent);
                break;
            }
            case MODAL_SUBMIT: {
                ModalSubmitEventImpl modalSubmitEvent = new ModalSubmitEventImpl(interaction);
                this.api.getEventDispatcher().dispatchModalSubmitEvent(server == null ? this.api : server, (Server)server, (TextChannel)interaction.getChannel().orElse(null), interaction.getUser(), (ModalSubmitEvent)modalSubmitEvent);
                break;
            }
        }
    }
}

