/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.logging;

import org.apache.logging.log4j.util.BiConsumer;
import org.apache.logging.log4j.util.PropertySource;

public class Log4jPropertySource
implements PropertySource {
    @Override
    public int getPriority() {
        return Integer.MIN_VALUE;
    }

    @Override
    public void forEach(BiConsumer<String, String> action) {
        action.accept("log4j.isThreadContextMapInheritable", "true");
    }

    @Override
    public CharSequence getNormalForm(Iterable<? extends CharSequence> tokens) {
        return "log4j." + PropertySource.Util.joinAsCamelCase(tokens);
    }
}

