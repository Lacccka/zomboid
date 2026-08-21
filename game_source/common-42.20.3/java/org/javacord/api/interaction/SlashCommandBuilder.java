/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.interaction;

import java.util.List;
import org.javacord.api.interaction.ApplicationCommandBuilder;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.internal.SlashCommandBuilderDelegate;
import org.javacord.api.util.internal.DelegateFactory;

public class SlashCommandBuilder
extends ApplicationCommandBuilder<SlashCommand, SlashCommandBuilderDelegate, SlashCommandBuilder> {
    private final SlashCommandBuilderDelegate delegate = (SlashCommandBuilderDelegate)super.getDelegate();

    public SlashCommandBuilder() {
        super(DelegateFactory.createSlashCommandBuilderDelegate());
    }

    public SlashCommandBuilder addOption(SlashCommandOption option) {
        this.delegate.addOption(option);
        return this;
    }

    public SlashCommandBuilder setOptions(List<SlashCommandOption> options) {
        this.delegate.setOptions(options);
        return this;
    }
}

