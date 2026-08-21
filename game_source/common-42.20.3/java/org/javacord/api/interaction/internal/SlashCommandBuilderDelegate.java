/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.interaction.internal;

import java.util.List;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.internal.ApplicationCommandBuilderDelegate;

public interface SlashCommandBuilderDelegate
extends ApplicationCommandBuilderDelegate<SlashCommand> {
    public void addOption(SlashCommandOption var1);

    public void setOptions(List<SlashCommandOption> var1);
}

