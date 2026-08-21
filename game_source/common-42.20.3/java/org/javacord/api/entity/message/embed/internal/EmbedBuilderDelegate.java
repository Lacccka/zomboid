/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.message.embed.internal;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.time.Instant;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.EditableEmbedField;
import org.javacord.api.entity.message.embed.EmbedField;
import org.javacord.api.entity.user.User;

public interface EmbedBuilderDelegate {
    public void setTitle(String var1);

    public void setDescription(String var1);

    public void setUrl(String var1);

    public void setTimestampToNow();

    public void setTimestamp(Instant var1);

    public void setColor(Color var1);

    public void setFooter(String var1);

    public void setFooter(String var1, String var2);

    public void setFooter(String var1, Icon var2);

    public void setFooter(String var1, File var2);

    public void setFooter(String var1, InputStream var2);

    public void setFooter(String var1, InputStream var2, String var3);

    public void setFooter(String var1, byte[] var2);

    public void setFooter(String var1, byte[] var2, String var3);

    public void setFooter(String var1, BufferedImage var2);

    public void setFooter(String var1, BufferedImage var2, String var3);

    public void setImage(String var1);

    public void setImage(Icon var1);

    public void setImage(File var1);

    public void setImage(InputStream var1);

    public void setImage(InputStream var1, String var2);

    public void setImage(byte[] var1);

    public void setImage(byte[] var1, String var2);

    public void setImage(BufferedImage var1);

    public void setImage(BufferedImage var1, String var2);

    public void setAuthor(MessageAuthor var1);

    public void setAuthor(User var1);

    public void setAuthor(String var1);

    public void setAuthor(String var1, String var2, String var3);

    public void setAuthor(String var1, String var2, Icon var3);

    public void setAuthor(String var1, String var2, File var3);

    public void setAuthor(String var1, String var2, InputStream var3);

    public void setAuthor(String var1, String var2, InputStream var3, String var4);

    public void setAuthor(String var1, String var2, byte[] var3);

    public void setAuthor(String var1, String var2, byte[] var3, String var4);

    public void setAuthor(String var1, String var2, BufferedImage var3);

    public void setAuthor(String var1, String var2, BufferedImage var3, String var4);

    public void setThumbnail(String var1);

    public void setThumbnail(Icon var1);

    public void setThumbnail(File var1);

    public void setThumbnail(InputStream var1);

    public void setThumbnail(InputStream var1, String var2);

    public void setThumbnail(byte[] var1);

    public void setThumbnail(byte[] var1, String var2);

    public void setThumbnail(BufferedImage var1);

    public void setThumbnail(BufferedImage var1, String var2);

    public void addField(String var1, String var2, boolean var3);

    public void updateFields(Predicate<EmbedField> var1, Consumer<EditableEmbedField> var2);

    public void removeFields(Predicate<EmbedField> var1);

    public boolean requiresAttachments();
}

