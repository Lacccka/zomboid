/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.interaction;

import java.util.List;
import org.javacord.api.interaction.ApplicationCommandUpdater;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.internal.SlashCommandUpdaterDelegate;
import org.javacord.api.util.internal.DelegateFactory;

public class SlashCommandUpdater
extends ApplicationCommandUpdater<SlashCommand, SlashCommandUpdaterDelegate, SlashCommandUpdater> {
    private final SlashCommandUpdaterDelegate delegate = (SlashCommandUpdaterDelegate)super.getDelegate();

    public SlashCommandUpdater(long commandId) {
        super(DelegateFactory.createSlashCommandUpdaterDelegate(commandId));
    }

    public SlashCommandUpdater setSlashCommandOptions(List<SlashCommandOption> slashCommandOptions) {
        this.delegate.setOptions(slashCommandOptions);
        return this;
    }
}

