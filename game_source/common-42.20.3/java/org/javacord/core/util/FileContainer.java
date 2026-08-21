/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import javax.imageio.ImageIO;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.Javacord;
import org.javacord.api.entity.Icon;
import org.javacord.core.util.io.FileUtils;
import org.javacord.core.util.logging.LoggerUtil;

public class FileContainer {
    private static final Logger logger = LoggerUtil.getLogger(FileContainer.class);
    private final BufferedImage fileAsBufferedImage;
    private final File fileAsFile;
    private final Icon fileAsIcon;
    private final URL fileAsUrl;
    private final byte[] fileAsByteArray;
    private final InputStream fileAsInputStream;
    private String fileTypeOrName;
    private String fileDescription;

    public FileContainer(BufferedImage file, String type) {
        this(file, type, null);
    }

    public FileContainer(BufferedImage file, String type, String description) {
        this.fileAsBufferedImage = file;
        this.fileAsFile = null;
        this.fileAsIcon = null;
        this.fileAsUrl = null;
        this.fileAsByteArray = null;
        this.fileAsInputStream = null;
        this.setFileTypeOrName(type);
        this.fileDescription = description;
    }

    public FileContainer(File file) {
        this(file, false, null);
    }

    public FileContainer(File file, boolean isSpoiler) {
        this(file, isSpoiler, null);
    }

    public FileContainer(File file, String description) {
        this(file, false, description);
    }

    public FileContainer(File file, boolean isSpoiler, String description) {
        this.fileAsBufferedImage = null;
        this.fileAsFile = file;
        this.fileAsIcon = null;
        this.fileAsUrl = null;
        this.fileAsByteArray = null;
        this.fileAsInputStream = null;
        this.fileTypeOrName = (isSpoiler ? "SPOILER_" : "") + file.getName();
        this.fileDescription = description;
    }

    public FileContainer(Icon file) {
        this(file, false);
    }

    public FileContainer(Icon file, boolean isSpoiler) {
        this(file, isSpoiler, null);
    }

    public FileContainer(Icon file, String description) {
        this(file, false, description);
    }

    public FileContainer(Icon file, boolean isSpoiler, String description) {
        this.fileAsBufferedImage = null;
        this.fileAsFile = null;
        this.fileAsIcon = file;
        this.fileAsUrl = null;
        this.fileAsByteArray = null;
        this.fileAsInputStream = null;
        this.fileTypeOrName = (isSpoiler ? "SPOILER_" : "") + file.getUrl().getFile();
    }

    public FileContainer(URL file) {
        this(file, false);
    }

    public FileContainer(URL file, String description) {
        this(file, false, description);
    }

    public FileContainer(URL file, boolean isSpoiler) {
        this(file, isSpoiler, null);
    }

    public FileContainer(URL file, boolean isSpoiler, String description) {
        this.fileAsBufferedImage = null;
        this.fileAsFile = null;
        this.fileAsIcon = null;
        this.fileAsUrl = file;
        this.fileAsByteArray = null;
        this.fileAsInputStream = null;
        this.fileTypeOrName = (isSpoiler ? "SPOILER_" : "") + new File(file.getFile()).getName();
        this.fileDescription = description;
    }

    public FileContainer(byte[] file, String type) {
        this(file, type, null);
    }

    public FileContainer(byte[] file, String type, String description) {
        this.fileAsBufferedImage = null;
        this.fileAsFile = null;
        this.fileAsIcon = null;
        this.fileAsUrl = null;
        this.fileAsByteArray = file;
        this.fileAsInputStream = null;
        this.fileTypeOrName = type;
        this.fileDescription = description;
    }

    public FileContainer(InputStream file, String type) {
        this(file, type, null);
    }

    public FileContainer(InputStream file, String type, String description) {
        this.fileAsBufferedImage = null;
        this.fileAsFile = null;
        this.fileAsIcon = null;
        this.fileAsUrl = null;
        this.fileAsByteArray = null;
        this.fileAsInputStream = file;
        this.fileTypeOrName = type;
        this.fileDescription = description;
    }

    public void setFileTypeOrName(String type) {
        this.fileTypeOrName = type;
        if (this.fileAsBufferedImage != null && !ImageIO.getImageWritersByFormatName(this.getFileType()).hasNext()) {
            throw new IllegalArgumentException(String.format("No image writer found for format \"%s\"", this.getFileType()));
        }
    }

    public String getFileType() {
        if (this.fileTypeOrName != null && this.fileTypeOrName.contains(".")) {
            return FileUtils.getExtension(this.fileTypeOrName);
        }
        return this.fileTypeOrName;
    }

    public String getFileTypeOrName() {
        return this.fileTypeOrName;
    }

    public String getDescription() {
        return this.fileDescription;
    }

    public CompletableFuture<byte[]> asByteArray(DiscordApi api) {
        CompletableFuture<byte[]> future = new CompletableFuture<byte[]>();
        try {
            if (this.fileAsByteArray != null) {
                future.complete(this.fileAsByteArray);
                return future;
            }
            if (this.fileAsBufferedImage != null || this.fileAsFile != null || this.fileAsIcon != null || this.fileAsUrl != null || this.fileAsInputStream != null) {
                api.getThreadPool().getExecutorService().submit(() -> {
                    try (BufferedInputStream in = new BufferedInputStream(this.asInputStream(api));
                         ByteArrayOutputStream out = new ByteArrayOutputStream();){
                        int n;
                        byte[] buf = new byte[1024];
                        while (-1 != (n = ((InputStream)in).read(buf))) {
                            out.write(buf, 0, n);
                        }
                        future.complete(out.toByteArray());
                    }
                    catch (Throwable t) {
                        future.completeExceptionally(t);
                    }
                });
                return future;
            }
            future.completeExceptionally(new IllegalStateException("No file variant is set"));
        }
        catch (Throwable t) {
            future.completeExceptionally(t);
        }
        return future;
    }

    public InputStream asInputStream(DiscordApi api) throws IOException {
        if (this.fileAsBufferedImage != null) {
            PipedOutputStream pos = new PipedOutputStream();
            PipedInputStream pis = new PipedInputStream(pos);
            api.getThreadPool().getExecutorService().submit(() -> {
                try {
                    ImageIO.write((RenderedImage)this.fileAsBufferedImage, this.getFileType(), pos);
                    pos.close();
                }
                catch (Throwable t) {
                    logger.error("Failed to process buffered image file!", t);
                }
            });
            return pis;
        }
        if (this.fileAsFile != null) {
            return new FileInputStream(this.fileAsFile);
        }
        if (this.fileAsIcon != null || this.fileAsUrl != null) {
            URL url = this.fileAsUrl == null ? this.fileAsIcon.getUrl() : this.fileAsUrl;
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setRequestProperty("User-Agent", Javacord.USER_AGENT);
            return conn.getInputStream();
        }
        if (this.fileAsByteArray != null) {
            return new ByteArrayInputStream(this.fileAsByteArray);
        }
        if (this.fileAsInputStream != null) {
            return this.fileAsInputStream;
        }
        throw new IllegalStateException("No file variant is set");
    }

    public CompletableFuture<BufferedImage> asBufferedImage(DiscordApi api) {
        return ((CompletableFuture)this.asByteArray(api).thenApply(ByteArrayInputStream::new)).thenApply(stream -> {
            try {
                return ImageIO.read(stream);
            }
            catch (IOException e) {
                throw new CompletionException(e);
            }
        });
    }
}

