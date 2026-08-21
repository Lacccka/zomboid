/*
 * Decompiled with CFR 0.152.
 */
package com.neovisionaries.ws.client;

import com.neovisionaries.ws.client.ListenerManager;
import com.neovisionaries.ws.client.ThreadType;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketThread;

class ConnectThread
extends WebSocketThread {
    public ConnectThread(WebSocket ws) {
        super("ConnectThread", ws, ThreadType.CONNECT_THREAD);
    }

    public void runMain() {
        try {
            this.mWebSocket.connect();
        }
        catch (WebSocketException e) {
            this.handleError(e);
        }
    }

    private void handleError(WebSocketException cause) {
        ListenerManager manager = this.mWebSocket.getListenerManager();
        manager.callOnError(cause);
        manager.callOnConnectError(cause);
    }
}

