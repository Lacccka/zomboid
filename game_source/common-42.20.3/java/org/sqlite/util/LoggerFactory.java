/*
 * Decompiled with CFR 0.152.
 */
package org.sqlite.util;

import java.util.function.Supplier;
import java.util.logging.Level;
import org.sqlite.util.Logger;

public class LoggerFactory {
    static final boolean USE_SLF4J;

    public static Logger getLogger(Class<?> hostClass) {
        if (USE_SLF4J) {
            return new SLF4JLogger(hostClass);
        }
        return new JDKLogger(hostClass);
    }

    static {
        boolean useSLF4J;
        try {
            Class.forName("org.slf4j.Logger");
            useSLF4J = true;
        }
        catch (Exception e) {
            useSLF4J = false;
        }
        USE_SLF4J = useSLF4J;
    }

    private static class SLF4JLogger
    implements Logger {
        final org.slf4j.Logger logger;

        SLF4JLogger(Class<?> hostClass) {
            this.logger = org.slf4j.LoggerFactory.getLogger(hostClass);
        }

        @Override
        public void trace(Supplier<String> message) {
            if (this.logger.isTraceEnabled()) {
                this.logger.trace(message.get());
            }
        }

        @Override
        public void info(Supplier<String> message) {
            if (this.logger.isInfoEnabled()) {
                this.logger.info(message.get());
            }
        }

        @Override
        public void warn(Supplier<String> message) {
            if (this.logger.isWarnEnabled()) {
                this.logger.warn(message.get());
            }
        }

        @Override
        public void error(Supplier<String> message, Throwable t) {
            if (this.logger.isErrorEnabled()) {
                this.logger.error(message.get(), t);
            }
        }
    }

    private static class JDKLogger
    implements Logger {
        final java.util.logging.Logger logger;

        public JDKLogger(Class<?> hostClass) {
            this.logger = java.util.logging.Logger.getLogger(hostClass.getCanonicalName());
        }

        @Override
        public void trace(Supplier<String> message) {
            if (this.logger.isLoggable(Level.FINEST)) {
                this.logger.log(Level.FINEST, message.get());
            }
        }

        @Override
        public void info(Supplier<String> message) {
            if (this.logger.isLoggable(Level.INFO)) {
                this.logger.log(Level.INFO, message.get());
            }
        }

        @Override
        public void warn(Supplier<String> message) {
            if (this.logger.isLoggable(Level.WARNING)) {
                this.logger.log(Level.WARNING, message.get());
            }
        }

        @Override
        public void error(Supplier<String> message, Throwable t) {
            if (this.logger.isLoggable(Level.SEVERE)) {
                this.logger.log(Level.SEVERE, message.get(), t);
            }
        }
    }
}

