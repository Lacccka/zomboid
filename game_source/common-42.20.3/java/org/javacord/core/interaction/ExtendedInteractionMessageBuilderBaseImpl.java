/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.interaction;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.internal.InteractionMessageBuilderDelegate;
import org.javacord.api.interaction.InteractionBase;
import org.javacord.api.interaction.callback.ExtendedInteractionMessageBuilderBase;
import org.javacord.core.interaction.InteractionMessageBuilderBaseImpl;

public abstract class ExtendedInteractionMessageBuilderBaseImpl<T>
extends InteractionMessageBuilderBaseImpl<T>
implements ExtendedInteractionMessageBuilderBase<T> {
    public ExtendedInteractionMessageBuilderBaseImpl(Class<T> myClass) {
        super(myClass);
    }

    public ExtendedInteractionMessageBuilderBaseImpl(Class<T> myClass, InteractionMessageBuilderDelegate delegate) {
        super(myClass, delegate);
    }

    @Override
    public T copy(Message message) {
        this.delegate.copy(message);
        return this.myClass.cast(this);
    }

    @Override
    public T copy(InteractionBase interaction) {
        this.delegate.copy(interaction);
        return this.myClass.cast(this);
    }

    @Override
    public T addAttachment(BufferedImage image, String fileName) {
        this.addAttachment(image, fileName, null);
        return this.myClass.cast(this);
    }

    @Override
    public T addAttachment(BufferedImage image, String fileName, String description) {
        this.delegate.addAttachment(image, fileName, description);
        return this.myClass.cast(this);
    }

    @Override
    public T addAttachment(File file) {
        this.addAttachment(file, null);
        return this.myClass.cast(this);
    }

    @Override
    public T addAttachment(File file, String description) {
        this.delegate.addAttachment(file, description);
        return this.myClass.cast(this);
    }

    @Override
    public T addAttachment(Icon icon) {
        this.addAttachment(icon, null);
        return this.myClass.cast(this);
    }

    @Override
    public T addAttachment(Icon icon, String description) {
        this.delegate.addAttachment(icon, description);
        return this.myClass.cast(this);
    }

    @Override
    public T addAttachment(URL url) {
        this.addAttachment(url, null);
        return this.myClass.cast(this);
    }

    @Override
    public T addAttachment(URL url, String description) {
        this.delegate.addAttachment(url, description);
        return this.myClass.cast(this);
    }

    @Override
    public T addAttachment(byte[] bytes, String fileName) {
        this.addAttachment(bytes, fileName, null);
        return this.myClass.cast(this);
    }

    @Override
    public T addAttachment(byte[] bytes, String fileName, String description) {
        this.delegate.addAttachment(bytes, fileName, description);
        return this.myClass.cast(this);
    }

    @Override
    public T addAttachment(InputStream stream, String fileName) {
        this.addAttachment(stream, fileName, null);
        return this.myClass.cast(this);
    }

    @Override
    public T addAttachment(InputStream stream, String fileName, String description) {
        this.delegate.addAttachment(stream, fileName, description);
        return this.myClass.cast(this);
    }

    @Override
    public T addAttachmentAsSpoiler(BufferedImage image, String fileName) {
        this.addAttachment(image, "SPOILER_" + fileName, null);
        return this.myClass.cast(this);
    }

    @Override
    public T addAttachmentAsSpoiler(BufferedImage image, String fileName, String description) {
        this.delegate.addAttachment(image, "SPOILER_" + fileName, description);
        return this.myClass.cast(this);
    }

    @Override
    public T addAttachmentAsSpoiler(File file) {
        this.addAttachmentAsSpoiler(file, null);
        return this.myClass.cast(this);
    }

    @Override
    public T addAttachmentAsSpoiler(File file, String description) {
        this.delegate.addAttachmentAsSpoiler(file, description);
        return this.myClass.cast(this);
    }

    @Override
    public T addAttachmentAsSpoiler(Icon icon) {
        this.addAttachmentAsSpoiler(icon, null);
        return this.myClass.cast(this);
    }

    @Override
    public T addAttachmentAsSpoiler(Icon icon, String description) {
        this.delegate.addAttachmentAsSpoiler(icon, description);
        return this.myClass.cast(this);
    }

    @Override
    public T addAttachmentAsSpoiler(URL url) {
        this.addAttachmentAsSpoiler(url, null);
        return this.myClass.cast(this);
    }

    @Override
    public T addAttachmentAsSpoiler(URL url, String description) {
        this.delegate.addAttachmentAsSpoiler(url, description);
        return this.myClass.cast(this);
    }

    @Override
    public T addAttachmentAsSpoiler(byte[] bytes, String fileName) {
        this.addAttachment(bytes, "SPOILER_" + fileName, null);
        return this.myClass.cast(this);
    }

    @Override
    public T addAttachmentAsSpoiler(byte[] bytes, String fileName, String description) {
        this.delegate.addAttachment(bytes, "SPOILER_" + fileName, description);
        return this.myClass.cast(this);
    }

    @Override
    public T addAttachmentAsSpoiler(InputStream stream, String fileName) {
        this.addAttachment(stream, "SPOILER_" + fileName, null);
        return this.myClass.cast(this);
    }

    @Override
    public T addAttachmentAsSpoiler(InputStream stream, String fileName, String description) {
        this.delegate.addAttachment(stream, "SPOILER_" + fileName, description);
        return this.myClass.cast(this);
    }
}

