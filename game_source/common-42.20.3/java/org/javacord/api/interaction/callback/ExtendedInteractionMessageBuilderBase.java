/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.interaction.callback;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.message.Message;
import org.javacord.api.interaction.InteractionBase;
import org.javacord.api.interaction.callback.InteractionMessageBuilderBase;

public interface ExtendedInteractionMessageBuilderBase<T>
extends InteractionMessageBuilderBase<T> {
    public T copy(Message var1);

    public T copy(InteractionBase var1);

    public T addAttachment(BufferedImage var1, String var2);

    public T addAttachment(BufferedImage var1, String var2, String var3);

    public T addAttachment(File var1);

    public T addAttachment(File var1, String var2);

    public T addAttachment(Icon var1);

    public T addAttachment(Icon var1, String var2);

    public T addAttachment(URL var1);

    public T addAttachment(URL var1, String var2);

    public T addAttachment(byte[] var1, String var2);

    public T addAttachment(byte[] var1, String var2, String var3);

    public T addAttachment(InputStream var1, String var2);

    public T addAttachment(InputStream var1, String var2, String var3);

    public T addAttachmentAsSpoiler(BufferedImage var1, String var2);

    public T addAttachmentAsSpoiler(BufferedImage var1, String var2, String var3);

    public T addAttachmentAsSpoiler(File var1);

    public T addAttachmentAsSpoiler(File var1, String var2);

    public T addAttachmentAsSpoiler(Icon var1);

    public T addAttachmentAsSpoiler(Icon var1, String var2);

    public T addAttachmentAsSpoiler(URL var1);

    public T addAttachmentAsSpoiler(URL var1, String var2);

    public T addAttachmentAsSpoiler(byte[] var1, String var2);

    public T addAttachmentAsSpoiler(byte[] var1, String var2, String var3);

    public T addAttachmentAsSpoiler(InputStream var1, String var2);

    public T addAttachmentAsSpoiler(InputStream var1, String var2, String var3);
}

