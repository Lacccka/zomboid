/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.concurrent.ExecutorService;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.util.logging.LoggerUtil;

public abstract class PacketHandler {
    private static final Logger logger = LoggerUtil.getLogger(PacketHandler.class);
    protected final DiscordApiImpl api;
    private final String type;
    private final boolean async;
    private ExecutorService executorService;

    public PacketHandler(DiscordApi api, boolean async, String type) {
        this.api = (DiscordApiImpl)api;
        this.async = async;
        this.type = type;
        if (async) {
            this.executorService = api.getThreadPool().getSingleThreadExecutorService("Handlers Processor");
        }
    }

    public void handlePacket(JsonNode packet) {
        if (this.async) {
            this.executorService.submit(() -> {
                try {
                    this.handle(packet);
                }
                catch (Throwable t) {
                    logger.warn("Couldn't handle packet of type {}. Please contact the developer! (packet: {})", (Object)this.getType(), (Object)packet, (Object)t);
                }
            });
        } else {
            try {
                this.handle(packet);
            }
            catch (Throwable t) {
                logger.warn("Couldn't handle packet of type {}. Please contact the developer! (packet: {})", (Object)this.getType(), (Object)packet, (Object)t);
            }
        }
    }

    protected abstract void handle(JsonNode var1);

    public String getType() {
        return this.type;
    }

    public int hashCode() {
        return this.type.hashCode();
    }

    public boolean equals(Object obj) {
        return obj instanceof PacketHandler && ((PacketHandler)obj).getType().equals(this.getType());
    }
}

