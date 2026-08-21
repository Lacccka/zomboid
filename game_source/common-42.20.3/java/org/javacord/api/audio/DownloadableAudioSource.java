/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.audio;

import java.util.concurrent.CompletableFuture;
import org.javacord.api.audio.AudioSource;

public interface DownloadableAudioSource
extends AudioSource {
    public CompletableFuture<? extends DownloadableAudioSource> download();

    public boolean isFullyDownloaded();
}

