/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.interaction.callback;

import java.util.EnumSet;
import java.util.List;
import org.javacord.api.entity.Mentionable;
import org.javacord.api.entity.message.MessageDecoration;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.message.component.HighLevelComponent;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.message.mention.AllowedMentions;

public interface InteractionMessageBuilderBase<T> {
    public T appendCode(String var1, String var2);

    public T append(String var1, MessageDecoration ... var2);

    public T append(Mentionable var1);

    public T append(Object var1);

    public T appendNamedLink(String var1, String var2);

    public T appendNewLine();

    public T setContent(String var1);

    public T addEmbed(EmbedBuilder var1);

    public T addEmbeds(EmbedBuilder ... var1);

    public T addEmbeds(List<EmbedBuilder> var1);

    public T addComponents(HighLevelComponent ... var1);

    public T removeAllComponents();

    public T removeComponent(int var1);

    public T removeComponent(HighLevelComponent var1);

    public T removeEmbed(EmbedBuilder var1);

    public T removeEmbeds(EmbedBuilder ... var1);

    public T removeAllEmbeds();

    public T setTts(boolean var1);

    public T setAllowedMentions(AllowedMentions var1);

    public T setFlags(MessageFlag ... var1);

    public T setFlags(EnumSet<MessageFlag> var1);

    public StringBuilder getStringBuilder();
}

