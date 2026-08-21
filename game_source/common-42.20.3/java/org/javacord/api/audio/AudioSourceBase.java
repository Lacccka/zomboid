/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.audio;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.javacord.api.DiscordApi;
import org.javacord.api.audio.AudioSource;
import org.javacord.api.audio.AudioTransformer;
import org.javacord.api.audio.internal.AudioSourceBaseDelegate;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.audio.AudioSourceAttachableListener;
import org.javacord.api.listener.audio.AudioSourceFinishedListener;
import org.javacord.api.util.event.ListenerManager;
import org.javacord.api.util.internal.DelegateFactory;

public abstract class AudioSourceBase
implements AudioSource {
    private final AudioSourceBaseDelegate delegate;
    private final List<AudioTransformer> transformers = Collections.synchronizedList(new ArrayList());
    private volatile boolean muted = false;

    public AudioSourceBase(DiscordApi api) {
        if (api == null) {
            throw new IllegalArgumentException("api must not be null!");
        }
        this.delegate = DelegateFactory.createAudioSourceBaseDelegate(api);
    }

    public AudioSourceBaseDelegate getDelegate() {
        return this.delegate;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected final byte[] applyTransformers(byte[] frame) {
        if (frame == null) {
            return null;
        }
        List<AudioTransformer> list = this.transformers;
        synchronized (list) {
            for (AudioTransformer transformer : this.transformers) {
                frame = transformer.transform(this, frame);
            }
        }
        return frame;
    }

    @Override
    public final DiscordApi getApi() {
        return this.delegate.getApi();
    }

    @Override
    public final void addTransformer(AudioTransformer transformer) {
        this.transformers.add(transformer);
    }

    @Override
    public final boolean removeTransformer(AudioTransformer transformer) {
        return this.transformers.remove(transformer);
    }

    @Override
    public final List<AudioTransformer> getTransformers() {
        return Collections.unmodifiableList(new ArrayList<AudioTransformer>(this.transformers));
    }

    @Override
    public final void removeTransformers() {
        this.transformers.clear();
    }

    @Override
    public boolean isMuted() {
        return this.muted;
    }

    @Override
    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    @Override
    public boolean hasFinished() {
        return !this.hasNextFrame();
    }

    @Override
    public final ListenerManager<AudioSourceFinishedListener> addAudioSourceFinishedListener(AudioSourceFinishedListener listener) {
        return this.delegate.addAudioSourceFinishedListener(listener);
    }

    @Override
    public final List<AudioSourceFinishedListener> getAudioSourceFinishedListeners() {
        return this.delegate.getAudioSourceFinishedListeners();
    }

    @Override
    public final <T extends AudioSourceAttachableListener & ObjectAttachableListener> Collection<ListenerManager<T>> addAudioSourceAttachableListener(T listener) {
        return this.delegate.addAudioSourceAttachableListener(listener);
    }

    @Override
    public final <T extends AudioSourceAttachableListener & ObjectAttachableListener> void removeAudioSourceAttachableListener(T listener) {
        this.delegate.removeAudioSourceAttachableListener(listener);
    }

    @Override
    public final <T extends AudioSourceAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>> getAudioSourceAttachableListeners() {
        return this.delegate.getAudioSourceAttachableListeners();
    }

    @Override
    public final <T extends AudioSourceAttachableListener & ObjectAttachableListener> void removeListener(Class<T> listenerClass, T listener) {
        this.delegate.removeListener(listenerClass, listener);
    }
}

