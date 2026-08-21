/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.input;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import org.apache.commons.io.build.AbstractStreamBuilder;
import org.apache.commons.io.input.ObservableInputStream;

public final class MessageDigestInputStream
extends ObservableInputStream {
    private final MessageDigest messageDigest;

    public static Builder builder() {
        return new Builder();
    }

    private MessageDigestInputStream(InputStream inputStream2, MessageDigest messageDigest) {
        super(inputStream2, new MessageDigestMaintainingObserver(messageDigest));
        this.messageDigest = messageDigest;
    }

    public MessageDigest getMessageDigest() {
        return this.messageDigest;
    }

    public static class Builder
    extends AbstractStreamBuilder<MessageDigestInputStream, Builder> {
        private MessageDigest messageDigest;

        @Override
        public MessageDigestInputStream get() throws IOException {
            return new MessageDigestInputStream(this.getInputStream(), this.messageDigest);
        }

        public Builder setMessageDigest(MessageDigest messageDigest) {
            this.messageDigest = messageDigest;
            return this;
        }

        public Builder setMessageDigest(String algorithm) throws NoSuchAlgorithmException {
            this.messageDigest = MessageDigest.getInstance(algorithm);
            return this;
        }
    }

    public static class MessageDigestMaintainingObserver
    extends ObservableInputStream.Observer {
        private final MessageDigest messageDigest;

        public MessageDigestMaintainingObserver(MessageDigest messageDigest) {
            this.messageDigest = Objects.requireNonNull(messageDigest, "messageDigest");
        }

        @Override
        public void data(byte[] input, int offset, int length) throws IOException {
            this.messageDigest.update(input, offset, length);
        }

        @Override
        public void data(int input) throws IOException {
            this.messageDigest.update((byte)input);
        }
    }
}

