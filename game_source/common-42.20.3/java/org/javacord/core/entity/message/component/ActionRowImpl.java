/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.message.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.List;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.ComponentType;
import org.javacord.api.entity.message.component.LowLevelComponent;
import org.javacord.core.entity.message.component.ButtonImpl;
import org.javacord.core.entity.message.component.ComponentImpl;
import org.javacord.core.entity.message.component.SelectMenuImpl;
import org.javacord.core.entity.message.component.TextInputImpl;

public class ActionRowImpl
extends ComponentImpl
implements ActionRow {
    private final List<LowLevelComponent> components;

    public ActionRowImpl(JsonNode data) {
        super(ComponentType.ACTION_ROW);
        this.components = new ArrayList<LowLevelComponent>();
        if (data.has("components")) {
            for (JsonNode componentJson : data.get("components")) {
                int typeInt = componentJson.get("type").asInt();
                ComponentType type = ComponentType.fromId(typeInt);
                if (type == ComponentType.BUTTON) {
                    this.components.add(new ButtonImpl(componentJson));
                    continue;
                }
                if (type.isSelectMenuType()) {
                    this.components.add(new SelectMenuImpl(componentJson));
                    continue;
                }
                if (type == ComponentType.TEXT_INPUT) {
                    this.components.add(new TextInputImpl(componentJson));
                    continue;
                }
                throw new IllegalStateException(String.format("Couldn't parse the component of type '%d'. Please contact the developer!", typeInt));
            }
        }
    }

    @Override
    public ObjectNode toJsonNode() {
        ObjectNode object = JsonNodeFactory.instance.objectNode();
        return this.toJsonNode(object);
    }

    public ObjectNode toJsonNode(ObjectNode object) throws IllegalStateException {
        object.put("type", ComponentType.ACTION_ROW.value());
        if (this.components.isEmpty()) {
            object.putArray("components");
            return object;
        }
        ArrayNode componentsJson = JsonNodeFactory.instance.objectNode().arrayNode();
        for (LowLevelComponent component : this.components) {
            if (component.getType() == ComponentType.BUTTON) {
                componentsJson.add(((ButtonImpl)component).toJsonNode());
                continue;
            }
            if (component.getType().isSelectMenuType()) {
                componentsJson.add(((SelectMenuImpl)component).toJsonNode());
                continue;
            }
            if (component.getType() == ComponentType.TEXT_INPUT) {
                componentsJson.add(((TextInputImpl)component).toJsonNode());
                continue;
            }
            if (component.getType() == ComponentType.ACTION_ROW) {
                throw new IllegalStateException("An action row can not contain an action row.");
            }
            throw new IllegalStateException("An unknown component type was added.");
        }
        object.set("components", componentsJson);
        return object;
    }

    public ActionRowImpl(List<LowLevelComponent> data) {
        super(ComponentType.ACTION_ROW);
        this.components = data;
    }

    @Override
    public List<LowLevelComponent> getComponents() {
        return this.components;
    }
}

