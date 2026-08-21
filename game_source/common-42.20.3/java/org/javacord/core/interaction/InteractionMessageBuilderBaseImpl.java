/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.interaction;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import org.javacord.api.entity.Mentionable;
import org.javacord.api.entity.message.MessageDecoration;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.message.component.HighLevelComponent;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.message.internal.InteractionMessageBuilderDelegate;
import org.javacord.api.entity.message.mention.AllowedMentions;
import org.javacord.api.interaction.callback.InteractionMessageBuilderBase;
import org.javacord.api.util.internal.DelegateFactory;

public abstract class InteractionMessageBuilderBaseImpl<T>
implements InteractionMessageBuilderBase<T> {
    protected final InteractionMessageBuilderDelegate delegate;
    protected final Class<T> myClass;

    protected InteractionMessageBuilderBaseImpl(Class<T> myClass) {
        this.myClass = myClass;
        this.delegate = DelegateFactory.createInteractionMessageBuilderDelegate();
    }

    protected InteractionMessageBuilderBaseImpl(Class<T> myClass, InteractionMessageBuilderDelegate delegate) {
        this.myClass = myClass;
        this.delegate = delegate;
    }

    @Override
    public T appendCode(String language, String code) {
        this.delegate.appendCode(language, code);
        return this.myClass.cast(this);
    }

    @Override
    public T append(String message, MessageDecoration ... decorations) {
        this.delegate.append(message, decorations);
        return this.myClass.cast(this);
    }

    @Override
    public T append(Mentionable entity) {
        this.delegate.append(entity);
        return this.myClass.cast(this);
    }

    @Override
    public T append(Object object) {
        this.delegate.append(object);
        return this.myClass.cast(this);
    }

    @Override
    public T appendNamedLink(String name, String url) {
        this.delegate.appendNamedLink(name, url);
        return this.myClass.cast(this);
    }

    @Override
    public T appendNewLine() {
        this.delegate.appendNewLine();
        return this.myClass.cast(this);
    }

    @Override
    public T setContent(String content) {
        this.delegate.setContent(content);
        return this.myClass.cast(this);
    }

    @Override
    public T addEmbed(EmbedBuilder embed) {
        this.delegate.addEmbed(embed);
        return this.myClass.cast(this);
    }

    @Override
    public T addEmbeds(EmbedBuilder ... embeds) {
        this.delegate.addEmbeds(Arrays.asList(embeds));
        return this.myClass.cast(this);
    }

    @Override
    public T addEmbeds(List<EmbedBuilder> embeds) {
        this.delegate.addEmbeds(embeds);
        return this.myClass.cast(this);
    }

    @Override
    public T addComponents(HighLevelComponent ... components) {
        this.delegate.addComponents(components);
        return this.myClass.cast(this);
    }

    @Override
    public T removeAllComponents() {
        this.delegate.removeAllComponents();
        return this.myClass.cast(this);
    }

    @Override
    public T removeComponent(int index) {
        this.delegate.removeComponent(index);
        return this.myClass.cast(this);
    }

    @Override
    public T removeComponent(HighLevelComponent builder) {
        this.delegate.removeComponent(builder);
        return this.myClass.cast(this);
    }

    @Override
    public T removeEmbed(EmbedBuilder embed) {
        this.delegate.removeEmbed(embed);
        return this.myClass.cast(this);
    }

    @Override
    public T removeEmbeds(EmbedBuilder ... embeds) {
        this.delegate.removeEmbeds(embeds);
        return this.myClass.cast(this);
    }

    @Override
    public T removeAllEmbeds() {
        this.delegate.removeAllEmbeds();
        return this.myClass.cast(this);
    }

    @Override
    public T setTts(boolean tts) {
        this.delegate.setTts(tts);
        return this.myClass.cast(this);
    }

    @Override
    public T setAllowedMentions(AllowedMentions allowedMentions) {
        this.delegate.setAllowedMentions(allowedMentions);
        return this.myClass.cast(this);
    }

    @Override
    public T setFlags(MessageFlag ... messageFlags) {
        this.setFlags(EnumSet.copyOf(Arrays.asList(messageFlags)));
        return this.myClass.cast(this);
    }

    @Override
    public T setFlags(EnumSet<MessageFlag> messageFlags) {
        this.delegate.setFlags(messageFlags);
        return this.myClass.cast(this);
    }

    @Override
    public StringBuilder getStringBuilder() {
        return this.delegate.getStringBuilder();
    }
}

