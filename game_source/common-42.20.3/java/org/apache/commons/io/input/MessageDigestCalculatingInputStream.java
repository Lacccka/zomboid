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

@Deprecated
public class MessageDigestCalculatingInputStream
extends ObservableInputStream {
    private static final String DEFAULT_ALGORITHM = "MD5";
    private final MessageDigest messageDigest;

    public static Builder builder() {
        return new Builder();
    }

    static MessageDigest getDefaultMessageDigest() throws NoSuchAlgorithmException {
        return MessageDigest.getInstance(DEFAULT_ALGORITHM);
    }

    @Deprecated
    public MessageDigestCalculatingInputStream(InputStream inputStream2) throws NoSuchAlgorithmException {
        this(inputStream2, MessageDigestCalculatingInputStream.getDefaultMessageDigest());
    }

    @Deprecated
    public MessageDigestCalculatingInputStream(InputStream inputStream2, MessageDigest messageDigest) {
        super(inputStream2, new MessageDigestMaintainingObserver(messageDigest));
        this.messageDigest = messageDigest;
    }

    @Deprecated
    public MessageDigestCalculatingInputStream(InputStream inputStream2, String algorithm) throws NoSuchAlgorithmException {
        this(inputStream2, MessageDigest.getInstance(algorithm));
    }

    public MessageDigest getMessageDigest() {
        return this.messageDigest;
    }

    public static class Builder
    extends AbstractStreamBuilder<MessageDigestCalculatingInputStream, Builder> {
        private MessageDigest messageDigest;

        public Builder() {
            try {
                this.messageDigest = MessageDigestCalculatingInputStream.getDefaultMessageDigest();
            }
            catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public MessageDigestCalculatingInputStream get() throws IOException {
            return new MessageDigestCalculatingInputStream(this.getInputStream(), this.messageDigest);
        }

        public void setMessageDigest(MessageDigest messageDigest) {
            this.messageDigest = messageDigest;
        }

        public void setMessageDigest(String algorithm) throws NoSuchAlgorithmException {
            this.messageDigest = MessageDigest.getInstance(algorithm);
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

