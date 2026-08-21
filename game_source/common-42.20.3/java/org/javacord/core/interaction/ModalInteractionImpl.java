/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.interaction;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.ComponentType;
import org.javacord.api.entity.message.component.HighLevelComponent;
import org.javacord.api.entity.message.component.LowLevelComponent;
import org.javacord.api.entity.message.component.TextInput;
import org.javacord.api.interaction.InteractionType;
import org.javacord.api.interaction.ModalInteraction;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.entity.message.component.ActionRowImpl;
import org.javacord.core.interaction.InteractionImpl;

public class ModalInteractionImpl
extends InteractionImpl
implements ModalInteraction {
    private final String customId;
    private final List<HighLevelComponent> components = new ArrayList<HighLevelComponent>();

    public ModalInteractionImpl(DiscordApiImpl api, TextChannel channel, JsonNode jsonData) {
        super(api, channel, jsonData);
        JsonNode data = jsonData.get("data");
        this.customId = data.get("custom_id").asText();
        block3: for (JsonNode jsonNode : data.get("components")) {
            switch (ComponentType.fromId(jsonNode.get("type").asInt())) {
                case ACTION_ROW: {
                    this.components.add(new ActionRowImpl(jsonNode));
                    continue block3;
                }
            }
            throw new IllegalStateException("Received a HighLevelComponent not handled in modals");
        }
    }

    @Override
    public InteractionType getType() {
        return InteractionType.MODAL_SUBMIT;
    }

    @Override
    public CompletableFuture<Void> respondWithModal(String customId, String title, List<HighLevelComponent> components) {
        throw new UnsupportedOperationException("This method is not supported by this interaction");
    }

    @Override
    public String getCustomId() {
        return this.customId;
    }

    @Override
    public List<HighLevelComponent> getComponents() {
        return this.components;
    }

    @Override
    public List<String> getTextInputValues() {
        return this.getComponents().stream().map(HighLevelComponent::asActionRow).filter(Optional::isPresent).map(Optional::get).map(ActionRow::getComponents).flatMap(Collection::stream).map(LowLevelComponent::asTextInput).filter(Optional::isPresent).map(Optional::get).map(TextInput::getValue).collect(Collectors.toList());
    }

    @Override
    public Optional<String> getTextInputValueByCustomId(String customId) {
        return this.getComponents().stream().map(HighLevelComponent::asActionRow).filter(Optional::isPresent).map(Optional::get).map(ActionRow::getComponents).flatMap(Collection::stream).map(LowLevelComponent::asTextInput).filter(Optional::isPresent).map(Optional::get).filter(textInput -> textInput.getCustomId().equals(customId)).map(TextInput::getValue).findFirst();
    }
}

