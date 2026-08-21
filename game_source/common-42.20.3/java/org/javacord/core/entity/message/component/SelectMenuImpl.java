/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.message.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.message.component.ComponentType;
import org.javacord.api.entity.message.component.SelectMenu;
import org.javacord.api.entity.message.component.SelectMenuOption;
import org.javacord.core.entity.message.component.ComponentImpl;
import org.javacord.core.entity.message.component.SelectMenuOptionImpl;

public class SelectMenuImpl
extends ComponentImpl
implements SelectMenu {
    private final List<SelectMenuOption> options;
    private final EnumSet<ChannelType> channelTypes;
    private final String placeholder;
    private final String customId;
    private final int minimumValues;
    private final int maximumValues;
    private final boolean isDisabled;

    public SelectMenuImpl(JsonNode data) {
        super(ComponentType.fromId(data.get("type").asInt()));
        this.options = new ArrayList<SelectMenuOption>();
        this.channelTypes = EnumSet.noneOf(ChannelType.class);
        this.customId = data.get("custom_id").asText();
        if (data.hasNonNull("options")) {
            for (JsonNode optionData : data.get("options")) {
                this.options.add(new SelectMenuOptionImpl(optionData));
            }
        }
        if (data.hasNonNull("channel_types")) {
            data.get("channel_types").forEach(channelType -> this.channelTypes.add(ChannelType.fromId(channelType.asInt())));
        }
        this.placeholder = data.has("placeholder") ? data.get("placeholder").asText() : null;
        this.minimumValues = data.has("min_values") ? data.get("min_values").asInt() : 1;
        this.maximumValues = data.has("max_values") ? data.get("max_values").asInt() : 1;
        this.isDisabled = data.has("disabled") && data.get("disabled").asBoolean();
    }

    public SelectMenuImpl(ComponentType type, List<SelectMenuOption> selectMenuOptions, String placeholder, String customId, int minimumValues, int maximumValues, boolean isDisabled, EnumSet<ChannelType> channelTypes) {
        super(type);
        this.options = selectMenuOptions;
        this.placeholder = placeholder;
        this.customId = customId;
        this.minimumValues = minimumValues;
        this.maximumValues = maximumValues;
        this.isDisabled = isDisabled;
        this.channelTypes = channelTypes;
    }

    @Override
    public EnumSet<ChannelType> getChannelTypes() {
        return EnumSet.copyOf(this.channelTypes);
    }

    @Override
    public Optional<String> getPlaceholder() {
        return Optional.ofNullable(this.placeholder);
    }

    @Override
    public String getCustomId() {
        return this.customId;
    }

    @Override
    public int getMinimumValues() {
        return this.minimumValues;
    }

    @Override
    public int getMaximumValues() {
        return this.maximumValues;
    }

    @Override
    public List<SelectMenuOption> getOptions() {
        return Collections.unmodifiableList(this.options);
    }

    @Override
    public boolean isDisabled() {
        return this.isDisabled;
    }

    @Override
    public ObjectNode toJsonNode() {
        ObjectNode object = JsonNodeFactory.instance.objectNode();
        return this.toJsonNode(object);
    }

    public ObjectNode toJsonNode(ObjectNode object) {
        ArrayNode arrayNode;
        object.put("type", this.getType().value());
        object.put("custom_id", this.customId);
        if (this.getType() == ComponentType.SELECT_MENU_STRING) {
            arrayNode = JsonNodeFactory.instance.arrayNode();
            this.options.forEach(option -> arrayNode.add(((SelectMenuOptionImpl)option).toJson()));
            object.set("options", arrayNode);
        }
        if (this.getType() == ComponentType.SELECT_MENU_CHANNEL) {
            arrayNode = JsonNodeFactory.instance.arrayNode();
            this.channelTypes.forEach(channelType -> arrayNode.add(channelType.getId()));
            object.set("channel_types", arrayNode);
        }
        object.put("min_values", this.minimumValues);
        object.put("max_values", this.maximumValues);
        object.put("disabled", this.isDisabled);
        if (this.placeholder != null) {
            object.put("placeholder", this.placeholder);
        }
        return object;
    }
}

