/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.utils;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MultiReadOnlySeekableByteChannel
implements SeekableByteChannel {
    private static final Path[] EMPTY_PATH_ARRAY = new Path[0];
    private final List<SeekableByteChannel> channelList;
    private long globalPosition;
    private int currentChannelIdx;

    public static SeekableByteChannel forFiles(File ... files) throws IOException {
        ArrayList<Path> paths = new ArrayList<Path>();
        for (File f : Objects.requireNonNull(files, "files")) {
            paths.add(f.toPath());
        }
        return MultiReadOnlySeekableByteChannel.forPaths(paths.toArray(EMPTY_PATH_ARRAY));
    }

    public static SeekableByteChannel forPaths(Path ... paths) throws IOException {
        ArrayList<SeekableByteChannel> channels = new ArrayList<SeekableByteChannel>();
        for (Path path : Objects.requireNonNull(paths, "paths")) {
            channels.add(Files.newByteChannel(path, StandardOpenOption.READ));
        }
        if (channels.size() == 1) {
            return (SeekableByteChannel)channels.get(0);
        }
        return new MultiReadOnlySeekableByteChannel(channels);
    }

    public static SeekableByteChannel forSeekableByteChannels(SeekableByteChannel ... channels) {
        if (Objects.requireNonNull(channels, "channels").length == 1) {
            return channels[0];
        }
        return new MultiReadOnlySeekableByteChannel(Arrays.asList(channels));
    }

    public MultiReadOnlySeekableByteChannel(List<SeekableByteChannel> channels) {
        this.channelList = Collections.unmodifiableList(new ArrayList(Objects.requireNonNull(channels, "channels")));
    }

    @Override
    public void close() throws IOException {
        IOException first = null;
        for (SeekableByteChannel ch : this.channelList) {
            try {
                ch.close();
            }
            catch (IOException ex) {
                if (first != null) continue;
                first = ex;
            }
        }
        if (first != null) {
            throw new IOException("failed to close wrapped channel", first);
        }
    }

    @Override
    public boolean isOpen() {
        return this.channelList.stream().allMatch(Channel::isOpen);
    }

    @Override
    public long position() {
        return this.globalPosition;
    }

    @Override
    public synchronized SeekableByteChannel position(long newPosition) throws IOException {
        if (newPosition < 0L) {
            throw new IllegalArgumentException("Negative position: " + newPosition);
        }
        if (!this.isOpen()) {
            throw new ClosedChannelException();
        }
        this.globalPosition = newPosition;
        long pos = newPosition;
        for (int i = 0; i < this.channelList.size(); ++i) {
            long newChannelPos;
            SeekableByteChannel currentChannel = this.channelList.get(i);
            long size = currentChannel.size();
            if (pos == -1L) {
                newChannelPos = 0L;
            } else if (pos <= size) {
                this.currentChannelIdx = i;
                long tmp = pos;
                pos = -1L;
                newChannelPos = tmp;
            } else {
                pos -= size;
                newChannelPos = size;
            }
            currentChannel.position(newChannelPos);
        }
        return this;
    }

    public synchronized SeekableByteChannel position(long channelNumber, long relativeOffset) throws IOException {
        if (!this.isOpen()) {
            throw new ClosedChannelException();
        }
        long globalPosition = relativeOffset;
        int i = 0;
        while ((long)i < channelNumber) {
            globalPosition += this.channelList.get(i).size();
            ++i;
        }
        return this.position(globalPosition);
    }

    @Override
    public synchronized int read(ByteBuffer dst) throws IOException {
        if (!this.isOpen()) {
            throw new ClosedChannelException();
        }
        if (!dst.hasRemaining()) {
            return 0;
        }
        int totalBytesRead = 0;
        while (dst.hasRemaining() && this.currentChannelIdx < this.channelList.size()) {
            SeekableByteChannel currentChannel = this.channelList.get(this.currentChannelIdx);
            int newBytesRead = currentChannel.read(dst);
            if (newBytesRead == -1) {
                ++this.currentChannelIdx;
                continue;
            }
            if (currentChannel.position() >= currentChannel.size()) {
                ++this.currentChannelIdx;
            }
            totalBytesRead += newBytesRead;
        }
        if (totalBytesRead > 0) {
            this.globalPosition += (long)totalBytesRead;
            return totalBytesRead;
        }
        return -1;
    }

    @Override
    public long size() throws IOException {
        if (!this.isOpen()) {
            throw new ClosedChannelException();
        }
        long acc = 0L;
        for (SeekableByteChannel ch : this.channelList) {
            acc += ch.size();
        }
        return acc;
    }

    @Override
    public SeekableByteChannel truncate(long size) {
        throw new NonWritableChannelException();
    }

    @Override
    public int write(ByteBuffer src) {
        throw new NonWritableChannelException();
    }
}

