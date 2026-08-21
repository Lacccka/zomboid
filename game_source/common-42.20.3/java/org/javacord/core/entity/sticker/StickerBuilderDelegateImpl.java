/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.sticker;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.sticker.Sticker;
import org.javacord.api.entity.sticker.internal.StickerBuilderDelegate;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.entity.sticker.StickerImpl;
import org.javacord.core.util.FileContainer;
import org.javacord.core.util.logging.LoggerUtil;
import org.javacord.core.util.rest.RestEndpoint;
import org.javacord.core.util.rest.RestMethod;
import org.javacord.core.util.rest.RestRequest;

public class StickerBuilderDelegateImpl
implements StickerBuilderDelegate {
    private final Logger logger = LoggerUtil.getLogger(StickerBuilderDelegateImpl.class);
    private final DiscordApiImpl api;
    private final ServerImpl server;
    private String name;
    private String description;
    private String tags;
    private File file;

    public StickerBuilderDelegateImpl(ServerImpl server) {
        this.api = (DiscordApiImpl)server.getApi();
        this.server = server;
    }

    @Override
    public void copy(Sticker sticker) {
        String fileUrlString = "https://cdn.discordapp.comstickers/" + sticker.getId() + sticker.getFormatType().toString().toLowerCase();
        this.name = sticker.getName();
        this.description = sticker.getDescription();
        this.tags = sticker.getTags();
        try {
            Optional.of(new File(new URL(fileUrlString).toURI())).ifPresent(this::setFile);
        }
        catch (MalformedURLException | URISyntaxException exception) {
            this.logger.warn("Seems like the url of the sticker is malformed! Please contact the developer!", (Throwable)exception);
        }
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void setTags(String tags) {
        this.tags = tags;
    }

    @Override
    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public CompletableFuture<Sticker> create() {
        return this.create(null);
    }

    @Override
    public CompletableFuture<Sticker> create(String reason) {
        if (this.name == null) {
            throw new IllegalStateException("The name is no optional parameter.");
        }
        if (this.tags == null) {
            throw new IllegalStateException("The tags are no optional parameter.");
        }
        if (this.file == null) {
            throw new IllegalStateException("The file content is no optional parameter.");
        }
        if (this.file.length() > 512000L) {
            throw new IllegalStateException("The file is too large (must be smaller than 500 KB).");
        }
        FileContainer container = new FileContainer(this.file);
        if (!(container.getFileTypeOrName().endsWith("png") || container.getFileTypeOrName().endsWith("apng") || container.getFileTypeOrName().endsWith("json"))) {
            throw new IllegalStateException("The file must be an image.");
        }
        String mediaType = URLConnection.guessContentTypeFromName(container.getFileTypeOrName());
        if (mediaType == null) {
            mediaType = "application/octet-stream";
        }
        MultipartBody multipartBody = new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("name", this.name).addFormDataPart("description", this.description).addFormDataPart("tags", this.tags).addFormDataPart("file", this.file.getName(), RequestBody.create(MediaType.parse(mediaType), container.asByteArray(this.api).join())).build();
        return new RestRequest(this.api, RestMethod.POST, RestEndpoint.SERVER_STICKER).setUrlParameters(this.server.getIdAsString()).setMultipartBody(multipartBody).setAuditLogReason(reason).execute(result -> new StickerImpl(this.api, result.getJsonBody()));
    }
}

