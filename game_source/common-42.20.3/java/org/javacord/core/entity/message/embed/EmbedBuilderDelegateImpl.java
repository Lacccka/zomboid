/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.message.embed;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.EditableEmbedField;
import org.javacord.api.entity.message.embed.EmbedField;
import org.javacord.api.entity.message.embed.internal.EmbedBuilderDelegate;
import org.javacord.api.entity.user.User;
import org.javacord.core.entity.message.embed.EditableEmbedFieldImpl;
import org.javacord.core.entity.message.embed.EmbedFieldImpl;
import org.javacord.core.util.FileContainer;
import org.javacord.core.util.io.FileUtils;

public class EmbedBuilderDelegateImpl
implements EmbedBuilderDelegate {
    private String title = null;
    private String description = null;
    private String url = null;
    private Instant timestamp = null;
    private Color color = null;
    private String footerText = null;
    private String footerIconUrl = null;
    private FileContainer footerIconContainer = null;
    private String imageUrl = null;
    private FileContainer imageContainer = null;
    private String authorName = null;
    private String authorUrl = null;
    private String authorIconUrl = null;
    private FileContainer authorIconContainer = null;
    private String thumbnailUrl = null;
    private FileContainer thumbnailContainer = null;
    private final List<EmbedFieldImpl> fields = new ArrayList<EmbedFieldImpl>();

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public void setTimestampToNow() {
        this.timestamp = Instant.now();
    }

    @Override
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public void setFooter(String text) {
        this.footerText = text;
        this.footerIconUrl = null;
        this.footerIconContainer = null;
    }

    @Override
    public void setFooter(String text, String iconUrl) {
        this.footerText = text;
        this.footerIconUrl = iconUrl;
        this.footerIconContainer = null;
    }

    @Override
    public void setFooter(String text, Icon icon) {
        this.footerText = text;
        this.footerIconUrl = icon.getUrl().toString();
        this.footerIconContainer = null;
    }

    @Override
    public void setFooter(String text, File icon) {
        this.footerText = text;
        this.footerIconUrl = null;
        if (icon == null) {
            this.footerIconContainer = null;
        } else {
            this.footerIconContainer = new FileContainer(icon);
            this.footerIconContainer.setFileTypeOrName(UUID.randomUUID().toString() + "." + FileUtils.getExtension(icon));
        }
    }

    @Override
    public void setFooter(String text, InputStream icon) {
        this.setFooter(text, icon, "png");
    }

    @Override
    public void setFooter(String text, InputStream icon, String fileType) {
        this.footerText = text;
        this.footerIconUrl = null;
        if (icon == null) {
            this.footerIconContainer = null;
        } else {
            this.footerIconContainer = new FileContainer(icon, fileType);
            this.footerIconContainer.setFileTypeOrName(UUID.randomUUID().toString() + "." + fileType);
        }
    }

    @Override
    public void setFooter(String text, byte[] icon) {
        this.setFooter(text, icon, "png");
    }

    @Override
    public void setFooter(String text, byte[] icon, String fileType) {
        this.footerText = text;
        this.footerIconUrl = null;
        if (icon == null) {
            this.footerIconContainer = null;
        } else {
            this.footerIconContainer = new FileContainer(icon, fileType);
            this.footerIconContainer.setFileTypeOrName(UUID.randomUUID().toString() + "." + fileType);
        }
    }

    @Override
    public void setFooter(String text, BufferedImage icon) {
        this.setFooter(text, icon, "png");
    }

    @Override
    public void setFooter(String text, BufferedImage icon, String fileType) {
        this.footerText = text;
        this.footerIconUrl = null;
        if (icon == null) {
            this.footerIconContainer = null;
        } else {
            this.footerIconContainer = new FileContainer(icon, fileType);
            this.footerIconContainer.setFileTypeOrName(UUID.randomUUID().toString() + "." + fileType);
        }
    }

    @Override
    public void setImage(String url) {
        this.imageUrl = url;
        this.imageContainer = null;
    }

    @Override
    public void setImage(Icon image) {
        this.imageUrl = image.getUrl().toString();
        this.imageContainer = null;
    }

    @Override
    public void setImage(File image) {
        this.imageUrl = null;
        if (image == null) {
            this.imageContainer = null;
        } else {
            this.imageContainer = new FileContainer(image);
            this.imageContainer.setFileTypeOrName(UUID.randomUUID().toString() + "." + FileUtils.getExtension(image));
        }
    }

    @Override
    public void setImage(InputStream image) {
        this.setImage(image, "png");
    }

    @Override
    public void setImage(InputStream image, String fileType) {
        this.imageUrl = null;
        if (image == null) {
            this.imageContainer = null;
        } else {
            this.imageContainer = new FileContainer(image, fileType);
            this.imageContainer.setFileTypeOrName(UUID.randomUUID().toString() + "." + fileType);
        }
    }

    @Override
    public void setImage(byte[] image) {
        this.setImage(image, "png");
    }

    @Override
    public void setImage(byte[] image, String fileType) {
        this.imageUrl = null;
        if (image == null) {
            this.imageContainer = null;
        } else {
            this.imageContainer = new FileContainer(image, fileType);
            this.imageContainer.setFileTypeOrName(UUID.randomUUID().toString() + "." + fileType);
        }
    }

    @Override
    public void setImage(BufferedImage image) {
        this.setImage(image, "png");
    }

    @Override
    public void setImage(BufferedImage image, String fileType) {
        this.imageUrl = null;
        if (image == null) {
            this.imageContainer = null;
        } else {
            this.imageContainer = new FileContainer(image, fileType);
            this.imageContainer.setFileTypeOrName(UUID.randomUUID().toString() + "." + fileType);
        }
    }

    @Override
    public void setAuthor(MessageAuthor author) {
        this.authorName = author.getDisplayName();
        this.authorUrl = null;
        this.authorIconUrl = author.getAvatar().getUrl().toString();
        this.authorIconContainer = null;
    }

    @Override
    public void setAuthor(User author) {
        this.authorName = author.getName();
        this.authorUrl = null;
        this.authorIconUrl = author.getAvatar().getUrl().toString();
        this.authorIconContainer = null;
    }

    @Override
    public void setAuthor(String name) {
        this.authorName = name;
        this.authorUrl = null;
        this.authorIconUrl = null;
        this.authorIconContainer = null;
    }

    @Override
    public void setAuthor(String name, String url, String iconUrl) {
        this.authorName = name;
        this.authorUrl = url;
        this.authorIconUrl = iconUrl;
        this.authorIconContainer = null;
    }

    @Override
    public void setAuthor(String name, String url, Icon icon) {
        this.authorName = name;
        this.authorUrl = url;
        this.authorIconUrl = icon.getUrl().toString();
        this.authorIconContainer = null;
    }

    @Override
    public void setAuthor(String name, String url, File icon) {
        this.authorName = name;
        this.authorUrl = url;
        this.authorIconUrl = null;
        if (icon == null) {
            this.authorIconContainer = null;
        } else {
            this.authorIconContainer = new FileContainer(icon);
            this.authorIconContainer.setFileTypeOrName(UUID.randomUUID().toString() + "." + FileUtils.getExtension(icon));
        }
    }

    @Override
    public void setAuthor(String name, String url, InputStream icon) {
        this.setAuthor(name, url, icon, "png");
    }

    @Override
    public void setAuthor(String name, String url, InputStream icon, String fileType) {
        this.authorName = name;
        this.authorUrl = url;
        this.authorIconUrl = null;
        if (icon == null) {
            this.authorIconContainer = null;
        } else {
            this.authorIconContainer = new FileContainer(icon, fileType);
            this.authorIconContainer.setFileTypeOrName(UUID.randomUUID().toString() + "." + fileType);
        }
    }

    @Override
    public void setAuthor(String name, String url, byte[] icon) {
        this.setAuthor(name, url, icon, "png");
    }

    @Override
    public void setAuthor(String name, String url, byte[] icon, String fileType) {
        this.authorName = name;
        this.authorUrl = url;
        this.authorIconUrl = null;
        if (icon == null) {
            this.authorIconContainer = null;
        } else {
            this.authorIconContainer = new FileContainer(icon, fileType);
            this.authorIconContainer.setFileTypeOrName(UUID.randomUUID().toString() + "." + fileType);
        }
    }

    @Override
    public void setAuthor(String name, String url, BufferedImage icon) {
        this.setAuthor(name, url, icon, "png");
    }

    @Override
    public void setAuthor(String name, String url, BufferedImage icon, String fileType) {
        this.authorName = name;
        this.authorUrl = url;
        this.authorIconUrl = null;
        if (icon == null) {
            this.authorIconContainer = null;
        } else {
            this.authorIconContainer = new FileContainer(icon, fileType);
            this.authorIconContainer.setFileTypeOrName(UUID.randomUUID().toString() + "." + fileType);
        }
    }

    @Override
    public void setThumbnail(String url) {
        this.thumbnailUrl = url;
        this.thumbnailContainer = null;
    }

    @Override
    public void setThumbnail(Icon thumbnail) {
        this.thumbnailUrl = thumbnail.getUrl().toString();
        this.thumbnailContainer = null;
    }

    @Override
    public void setThumbnail(File thumbnail) {
        this.thumbnailUrl = null;
        if (thumbnail == null) {
            this.thumbnailContainer = null;
        } else {
            this.thumbnailContainer = new FileContainer(thumbnail);
            this.thumbnailContainer.setFileTypeOrName(UUID.randomUUID().toString() + "." + FileUtils.getExtension(thumbnail));
        }
    }

    @Override
    public void setThumbnail(InputStream thumbnail) {
        this.setThumbnail(thumbnail, "png");
    }

    @Override
    public void setThumbnail(InputStream thumbnail, String fileType) {
        this.thumbnailUrl = null;
        if (thumbnail == null) {
            this.thumbnailContainer = null;
        } else {
            this.thumbnailContainer = new FileContainer(thumbnail, fileType);
            this.thumbnailContainer.setFileTypeOrName(UUID.randomUUID().toString() + "." + fileType);
        }
    }

    @Override
    public void setThumbnail(byte[] thumbnail) {
        this.setThumbnail(thumbnail, "png");
    }

    @Override
    public void setThumbnail(byte[] thumbnail, String fileType) {
        this.thumbnailUrl = null;
        if (thumbnail == null) {
            this.thumbnailContainer = null;
        } else {
            this.thumbnailContainer = new FileContainer(thumbnail, fileType);
            this.thumbnailContainer.setFileTypeOrName(UUID.randomUUID().toString() + "." + fileType);
        }
    }

    @Override
    public void setThumbnail(BufferedImage thumbnail) {
        this.setThumbnail(thumbnail, "png");
    }

    @Override
    public void setThumbnail(BufferedImage thumbnail, String fileType) {
        this.thumbnailUrl = null;
        if (thumbnail == null) {
            this.thumbnailContainer = null;
        } else {
            this.thumbnailContainer = new FileContainer(thumbnail, fileType);
            this.thumbnailContainer.setFileTypeOrName(UUID.randomUUID().toString() + "." + fileType);
        }
    }

    @Override
    public void addField(String name, String value, boolean inline) {
        this.fields.add(new EmbedFieldImpl(name, value, inline));
    }

    @Override
    public void updateFields(Predicate<EmbedField> predicate, Consumer<EditableEmbedField> updater) {
        this.fields.stream().filter(predicate).map(EditableEmbedFieldImpl::new).forEach(updater.andThen(field -> ((EditableEmbedFieldImpl)field).clearDelegate()));
    }

    @Override
    public void removeFields(Predicate<EmbedField> predicate) {
        this.fields.removeIf(predicate);
    }

    @Override
    public boolean requiresAttachments() {
        return this.footerIconContainer != null || this.imageContainer != null || this.authorIconContainer != null || this.thumbnailContainer != null;
    }

    public List<FileContainer> getRequiredAttachments() {
        ArrayList<FileContainer> requiredAttachments = new ArrayList<FileContainer>();
        if (this.footerIconContainer != null) {
            requiredAttachments.add(this.footerIconContainer);
        }
        if (this.imageContainer != null) {
            requiredAttachments.add(this.imageContainer);
        }
        if (this.authorIconContainer != null) {
            requiredAttachments.add(this.authorIconContainer);
        }
        if (this.thumbnailContainer != null) {
            requiredAttachments.add(this.thumbnailContainer);
        }
        return requiredAttachments;
    }

    public ObjectNode toJsonNode() {
        ObjectNode object = JsonNodeFactory.instance.objectNode();
        return this.toJsonNode(object);
    }

    public ObjectNode toJsonNode(ObjectNode object) {
        object.put("type", "rich");
        if (this.title != null && !this.title.equals("")) {
            object.put("title", this.title);
        }
        if (this.description != null && !this.description.equals("")) {
            object.put("description", this.description);
        }
        if (this.url != null && !this.url.equals("")) {
            object.put("url", this.url);
        }
        if (this.color != null) {
            object.put("color", this.color.getRGB() & 0xFFFFFF);
        }
        if (this.timestamp != null) {
            object.put("timestamp", DateTimeFormatter.ISO_INSTANT.format(this.timestamp));
        }
        if (this.footerText != null && !this.footerText.equals("") || this.footerIconUrl != null && !this.footerIconUrl.equals("")) {
            ObjectNode footer = object.putObject("footer");
            if (this.footerText != null && !this.footerText.equals("")) {
                footer.put("text", this.footerText);
            }
            if (this.footerIconUrl != null && !this.footerIconUrl.equals("")) {
                footer.put("icon_url", this.footerIconUrl);
            }
            if (this.footerIconContainer != null) {
                footer.put("icon_url", "attachment://" + this.footerIconContainer.getFileTypeOrName());
            }
        }
        if (this.imageUrl != null && !this.imageUrl.equals("")) {
            object.putObject("image").put("url", this.imageUrl);
        }
        if (this.imageContainer != null) {
            object.putObject("image").put("url", "attachment://" + this.imageContainer.getFileTypeOrName());
        }
        if (this.authorName != null && !this.authorName.equals("")) {
            ObjectNode author = object.putObject("author");
            author.put("name", this.authorName);
            if (this.authorUrl != null && !this.authorUrl.equals("")) {
                author.put("url", this.authorUrl);
            }
            if (this.authorIconUrl != null && !this.authorIconUrl.equals("")) {
                author.put("icon_url", this.authorIconUrl);
            }
            if (this.authorIconContainer != null) {
                author.put("icon_url", "attachment://" + this.authorIconContainer.getFileTypeOrName());
            }
        }
        if (this.thumbnailUrl != null && !this.thumbnailUrl.equals("")) {
            object.putObject("thumbnail").put("url", this.thumbnailUrl);
        }
        if (this.thumbnailContainer != null) {
            object.putObject("thumbnail").put("url", "attachment://" + this.thumbnailContainer.getFileTypeOrName());
        }
        if (this.fields.size() > 0) {
            ArrayNode jsonFields = object.putArray("fields");
            for (EmbedField embedField : this.fields) {
                ObjectNode jsonField = jsonFields.addObject();
                jsonField.put("name", embedField.getName());
                jsonField.put("value", embedField.getValue());
                jsonField.put("inline", embedField.isInline());
            }
        }
        return object;
    }
}

