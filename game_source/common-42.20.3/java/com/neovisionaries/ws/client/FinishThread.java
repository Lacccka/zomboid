/*
 * Decompiled with CFR 0.152.
 */
package com.neovisionaries.ws.client;

import com.neovisionaries.ws.client.ThreadType;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketThread;

class FinishThread
extends WebSocketThread {
    public FinishThread(WebSocket ws) {
        super("FinishThread", ws, ThreadType.FINISH_THREAD);
    }

    public void runMain() {
        this.mWebSocket.finish();
    }
}

