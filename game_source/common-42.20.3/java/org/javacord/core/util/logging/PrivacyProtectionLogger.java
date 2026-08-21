/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.logging;

import java.util.HashSet;
import java.util.Set;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.spi.AbstractLogger;

public class PrivacyProtectionLogger
extends AbstractLogger {
    private static final String PRIVATE_DATA_REPLACEMENT = "**********";
    private static final Set<String> privateDataSet = new HashSet<String>();
    private final Logger delegate;

    PrivacyProtectionLogger(Logger delegate) {
        this.delegate = delegate;
    }

    public static void addPrivateData(String privateData) {
        if (privateData != null && !privateData.trim().isEmpty()) {
            privateDataSet.add(privateData);
        }
    }

    @Override
    public void logMessage(String fqcn, Level level, Marker marker, Message message, Throwable t) {
        String formattedMessage = message.getFormattedMessage();
        if (privateDataSet.stream().anyMatch(formattedMessage::contains)) {
            this.delegate.log(level, marker, privateDataSet.stream().reduce(formattedMessage, (s, privateData) -> s.replace((CharSequence)privateData, PRIVATE_DATA_REPLACEMENT)), t);
        } else {
            this.delegate.log(level, marker, message, t);
        }
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, Message message, Throwable t) {
        return this.delegate.isEnabled(level, marker);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, CharSequence message, Throwable t) {
        return this.delegate.isEnabled(level, marker);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, Object message, Throwable t) {
        return this.delegate.isEnabled(level, marker);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Throwable t) {
        return this.delegate.isEnabled(level, marker);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message) {
        return this.delegate.isEnabled(level, marker);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object ... params) {
        return this.delegate.isEnabled(level, marker);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0) {
        return this.delegate.isEnabled(level, marker);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1) {
        return this.delegate.isEnabled(level, marker);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2) {
        return this.delegate.isEnabled(level, marker);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3) {
        return this.delegate.isEnabled(level, marker);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        return this.delegate.isEnabled(level, marker);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        return this.delegate.isEnabled(level, marker);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {
        return this.delegate.isEnabled(level, marker);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {
        return this.delegate.isEnabled(level, marker);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) {
        return this.delegate.isEnabled(level, marker);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {
        return this.delegate.isEnabled(level, marker);
    }

    @Override
    public Level getLevel() {
        return this.delegate.getLevel();
    }

    @Override
    public String getName() {
        return this.delegate.getName();
    }
}

