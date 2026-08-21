/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.logging;

import com.neovisionaries.ws.client.ThreadType;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketListener;
import com.neovisionaries.ws.client.WebSocketState;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.Logger;
import org.javacord.core.util.logging.LoggerUtil;

public class WebSocketLogger
implements WebSocketListener {
    private static final Logger logger = LoggerUtil.getLogger(WebSocketLogger.class);

    @Override
    public void onStateChanged(WebSocket websocket, WebSocketState newState) {
        logger.trace("onStateChanged: newState='{}'", (Object)newState);
    }

    @Override
    public void onConnectError(WebSocket websocket, WebSocketException cause) {
        logger.trace("onConnectError", (Throwable)cause);
    }

    @Override
    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) {
        logger.trace("onDisconnected: closedByServer='{}' serverCloseFrame='{}' clientCloseFrame='{}'", (Object)closedByServer, (Object)serverCloseFrame, (Object)clientCloseFrame);
    }

    @Override
    public void onFrame(WebSocket websocket, WebSocketFrame frame) {
        logger.trace("onFrame: frame='{}'", (Object)frame);
    }

    @Override
    public void onContinuationFrame(WebSocket websocket, WebSocketFrame frame) {
        logger.trace("onContinuationFrame: frame='{}'", (Object)frame);
    }

    @Override
    public void onTextFrame(WebSocket websocket, WebSocketFrame frame) {
        logger.trace("onTextFrame: frame='{}'", (Object)frame);
    }

    @Override
    public void onBinaryFrame(WebSocket websocket, WebSocketFrame frame) {
        logger.trace("onBinaryFrame: frame='{}'", (Object)frame);
    }

    @Override
    public void onCloseFrame(WebSocket websocket, WebSocketFrame frame) {
        logger.trace("onCloseFrame: frame='{}'", (Object)frame);
    }

    @Override
    public void onPingFrame(WebSocket websocket, WebSocketFrame frame) {
        logger.trace("onPingFrame: frame='{}'", (Object)frame);
    }

    @Override
    public void onPongFrame(WebSocket websocket, WebSocketFrame frame) {
        logger.trace("onPongFrame: frame='{}'", (Object)frame);
    }

    @Override
    public void onTextMessage(WebSocket websocket, String text) {
        logger.trace("onTextMessage: text='{}'", (Object)text);
    }

    @Override
    public void onTextMessage(WebSocket websocket, byte[] data) {
        logger.trace("onTextFrame: data='{}'", (Object)data);
    }

    @Override
    public void onBinaryMessage(WebSocket websocket, byte[] binary) {
        logger.trace("onBinaryMessage: binary='{}'", (Object)binary);
    }

    @Override
    public void onSendingFrame(WebSocket websocket, WebSocketFrame frame) {
        logger.trace("onSendingFrame: frame='{}'", (Object)frame);
    }

    @Override
    public void onFrameSent(WebSocket websocket, WebSocketFrame frame) {
        logger.trace("onFrameSent: frame='{}'", (Object)frame);
    }

    @Override
    public void onFrameUnsent(WebSocket websocket, WebSocketFrame frame) {
        logger.trace("onFrameUnsent: frame='{}'", (Object)frame);
    }

    @Override
    public void onThreadCreated(WebSocket websocket, ThreadType threadType, Thread thread2) {
        logger.trace("onThreadCreated: threadType='{}' thread='{}'", (Object)threadType, (Object)thread2);
    }

    @Override
    public void onThreadStarted(WebSocket websocket, ThreadType threadType, Thread thread2) {
        logger.trace("onThreadStarted: threadType='{}' thread='{}'", (Object)threadType, (Object)thread2);
    }

    @Override
    public void onThreadStopping(WebSocket websocket, ThreadType threadType, Thread thread2) {
        logger.trace("onThreadStopping: threadType='{}' thread='{}'", (Object)threadType, (Object)thread2);
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) {
        logger.trace("onConnected: headers='{}'", (Object)headers);
    }

    @Override
    public void onError(WebSocket websocket, WebSocketException cause) {
        logger.trace("onError", (Throwable)cause);
    }

    @Override
    public void onFrameError(WebSocket websocket, WebSocketException cause, WebSocketFrame frame) {
        logger.trace("onFrameError: frame='{}'", (Object)frame, (Object)cause);
    }

    @Override
    public void onMessageError(WebSocket websocket, WebSocketException cause, List<WebSocketFrame> frames) {
        logger.trace("onMessageError: frames='{}'", (Object)frames, (Object)cause);
    }

    @Override
    public void onMessageDecompressionError(WebSocket websocket, WebSocketException cause, byte[] compressed) {
        logger.trace("onMessageDecompressionError: compressed='{}'", (Object)compressed, (Object)cause);
    }

    @Override
    public void onTextMessageError(WebSocket websocket, WebSocketException cause, byte[] data) {
        logger.trace("onTextMessageError: data='{}'", (Object)data, (Object)cause);
    }

    @Override
    public void onSendError(WebSocket websocket, WebSocketException cause, WebSocketFrame frame) {
        logger.trace("onSendError: frame='{}'", (Object)frame, (Object)cause);
    }

    @Override
    public void onUnexpectedError(WebSocket websocket, WebSocketException cause) {
        logger.trace("onUnexpectedError", (Throwable)cause);
    }

    @Override
    public void handleCallbackError(WebSocket websocket, Throwable cause) {
        logger.trace("handleCallbackError", cause);
    }

    @Override
    public void onSendingHandshake(WebSocket websocket, String requestLine, List<String[]> headers) {
        logger.trace("onSendingHandshake: requestLine='{}' headers='{}'", (Object)requestLine, (Object)headers);
    }
}

