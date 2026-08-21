/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.interaction;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.javacord.api.interaction.ApplicationCommandType;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.interaction.ApplicationCommandImpl;
import org.javacord.core.interaction.SlashCommandOptionImpl;

public class SlashCommandImpl
extends ApplicationCommandImpl
implements SlashCommand {
    private final List<SlashCommandOption> options = new ArrayList<SlashCommandOption>();

    public SlashCommandImpl(DiscordApiImpl api, JsonNode data) {
        super(api, data);
        if (data.has("options")) {
            for (JsonNode optionJson : data.get("options")) {
                this.options.add(new SlashCommandOptionImpl(optionJson));
            }
        }
    }

    @Override
    public ApplicationCommandType getType() {
        return ApplicationCommandType.SLASH;
    }

    @Override
    public List<String> getFullCommandNames() {
        ArrayList<String> names = new ArrayList<String>();
        this.getNestedCommandNamesRecursive(this.getName(), this.getOptions(), names);
        return names;
    }

    private void getNestedCommandNamesRecursive(String preName, List<SlashCommandOption> options, List<String> names) {
        if (options.isEmpty()) {
            names.add(preName);
        } else {
            for (SlashCommandOption option : options) {
                if (option.isSubcommandOrGroup()) {
                    this.getNestedCommandNamesRecursive(preName + " " + option.getName(), option.getOptions(), names);
                    continue;
                }
                names.add(preName);
            }
        }
    }

    @Override
    public List<SlashCommandOption> getOptions() {
        return Collections.unmodifiableList(this.options);
    }
}

