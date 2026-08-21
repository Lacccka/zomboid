/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.gateway;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import org.apache.logging.log4j.Logger;
import org.javacord.api.audio.AudioSource;
import org.javacord.api.audio.AudioSourceBase;
import org.javacord.api.event.audio.AudioSourceFinishedEvent;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.audio.AudioConnectionImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.event.audio.AudioSourceFinishedEventImpl;
import org.javacord.core.util.event.DispatchQueueSelector;
import org.javacord.core.util.gateway.AudioPacket;
import org.javacord.core.util.logging.LoggerUtil;

public class AudioUdpSocket {
    private static final Logger logger = LoggerUtil.getLogger(AudioUdpSocket.class);
    private final DatagramSocket socket;
    private final String threadName;
    private final AudioConnectionImpl connection;
    private final InetSocketAddress address;
    private final int ssrc;
    private volatile boolean shouldSend = false;
    private byte[] secretKey;
    private char sequence = '\u0000';

    public AudioUdpSocket(AudioConnectionImpl connection, InetSocketAddress address, int ssrc) throws SocketException {
        this.connection = connection;
        this.address = address;
        this.ssrc = ssrc;
        this.socket = new DatagramSocket();
        this.threadName = String.format("Javacord Audio Send Thread (%#s)", connection.getServer());
    }

    public void setSecretKey(byte[] secretKey) {
        this.secretKey = secretKey;
    }

    public InetSocketAddress discoverIp() throws IOException {
        byte[] buffer = new byte[74];
        ByteBuffer.wrap(buffer).putShort((short)1).putShort((short)70).putInt(this.ssrc);
        this.socket.send(new DatagramPacket(buffer, buffer.length, this.address));
        buffer = new byte[74];
        this.socket.receive(new DatagramPacket(buffer, buffer.length));
        String ip = new String(buffer, 8, buffer.length - 10).trim();
        int port = ByteBuffer.wrap(new byte[]{buffer[buffer.length - 1], buffer[buffer.length - 2]}).getShort() & 0xFFFF;
        return new InetSocketAddress(ip, port);
    }

    public void startSending() {
        if (this.shouldSend) {
            return;
        }
        this.shouldSend = true;
        DiscordApiImpl api = (DiscordApiImpl)this.connection.getChannel().getApi();
        api.getThreadPool().getSingleThreadExecutorService(this.threadName).submit(() -> {
            block16: {
                try {
                    long nextFrameTimestamp = System.nanoTime();
                    boolean dontSleep = true;
                    boolean speaking = false;
                    long framesOfSilenceToPlay = 5L;
                    while (this.shouldSend) {
                        byte[] frame;
                        AudioSource source2 = this.connection.getCurrentAudioSourceBlocking();
                        if (source2 == null) {
                            logger.error("Got null audio source without being interrupted ({})", (Object)this.connection);
                            return;
                        }
                        if (source2.hasFinished()) {
                            this.connection.removeAudioSource();
                            dontSleep = true;
                            api.getEventDispatcher().dispatchAudioSourceFinishedEvent((DispatchQueueSelector)((ServerImpl)this.connection.getServer()), this.connection, ((AudioSourceBase)source2).getDelegate(), (AudioSourceFinishedEvent)new AudioSourceFinishedEventImpl(source2, this.connection));
                            continue;
                        }
                        AudioPacket packet = null;
                        byte[] byArray = frame = source2.hasNextFrame() ? source2.getNextFrame() : null;
                        if (source2.isMuted()) {
                            frame = null;
                        }
                        if (frame != null || framesOfSilenceToPlay > 0L) {
                            if (!speaking && frame != null) {
                                speaking = true;
                                this.connection.setSpeaking(true);
                            }
                            packet = new AudioPacket(frame, this.ssrc, this.sequence, this.sequence * 960);
                            if (frame == null) {
                                if (--framesOfSilenceToPlay == 0L) {
                                    speaking = false;
                                    this.connection.setSpeaking(false);
                                }
                            } else {
                                framesOfSilenceToPlay = 5L;
                            }
                        }
                        nextFrameTimestamp += 20000000L;
                        this.sequence = (char)(this.sequence + '\u0001');
                        if (packet != null) {
                            packet.encrypt(this.secretKey);
                        }
                        try {
                            if (dontSleep) {
                                nextFrameTimestamp = System.nanoTime() + 20000000L;
                                dontSleep = false;
                            } else {
                                Thread.sleep(Math.max(0L, nextFrameTimestamp - System.nanoTime()) / 1000000L);
                            }
                            if (packet == null) continue;
                            this.socket.send(packet.asUdpPacket(this.address));
                        }
                        catch (IOException e) {
                            logger.error("Failed to send audio packet for {}", (Object)this.connection);
                        }
                    }
                }
                catch (InterruptedException e) {
                    if (!this.shouldSend) break block16;
                    logger.debug("Got interrupted unexpectedly while waiting for next audio source packet");
                }
            }
        });
    }

    public void stopSending() {
        this.shouldSend = false;
        this.connection.getChannel().getApi().getThreadPool().removeAndShutdownSingleThreadExecutorService(this.threadName);
    }
}

