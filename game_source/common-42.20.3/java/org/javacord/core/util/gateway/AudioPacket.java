/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.gateway;

import com.codahale.xsalsa20poly1305.SecretBox;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import org.javacord.api.audio.SilentAudioSource;

public class AudioPacket {
    private static final byte RTP_TYPE = -128;
    private static final byte RTP_VERSION = 120;
    private static final int RTP_HEADER_LENGTH = 12;
    private static final int NONCE_LENGTH = 24;
    private boolean encrypted;
    private byte[] header;
    private byte[] audioFrame;

    public AudioPacket(int ssrc, char sequence, int timestamp) {
        this(null, ssrc, sequence, timestamp);
    }

    public AudioPacket(byte[] audioFrame, int ssrc, char sequence, int timestamp) {
        if (audioFrame == null) {
            audioFrame = SilentAudioSource.SILENCE_FRAME;
        }
        this.audioFrame = audioFrame;
        ByteBuffer buffer = ByteBuffer.allocate(12).put(0, (byte)-128).put(1, (byte)120).putChar(2, sequence).putInt(4, timestamp).putInt(8, ssrc);
        this.header = buffer.array();
    }

    public void encrypt(byte[] key) {
        byte[] nonce = new byte[24];
        System.arraycopy(this.header, 0, nonce, 0, 12);
        this.audioFrame = new SecretBox(key).seal(nonce, this.audioFrame);
        this.encrypted = true;
    }

    public DatagramPacket asUdpPacket(InetSocketAddress address) {
        byte[] packet = new byte[this.header.length + this.audioFrame.length];
        System.arraycopy(this.header, 0, packet, 0, this.header.length);
        System.arraycopy(this.audioFrame, 0, packet, this.header.length, this.audioFrame.length);
        return new DatagramPacket(packet, packet.length, address);
    }

    public boolean isEncrypted() {
        return this.encrypted;
    }
}

