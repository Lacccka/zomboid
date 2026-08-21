/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity;

import java.io.IOException;
import java.util.Arrays;
import java.util.Formattable;
import java.util.Formatter;
import org.javacord.api.util.logging.ExceptionLogger;

public interface Nameable
extends Formattable {
    public String getName();

    @Override
    default public void formatTo(Formatter formatter, int flags, int width, int precision) {
        boolean alternate = (flags & 4) != 0;
        String representation = alternate ? this.toString() : this.getName();
        boolean uppercase = (flags & 2) != 0;
        boolean leftAlign = (flags & 1) != 0;
        boolean doPad = representation.length() < width;
        String padString = null;
        if (doPad) {
            char[] spaces = new char[width - representation.length()];
            Arrays.fill(spaces, ' ');
            padString = new String(spaces);
        }
        try {
            if (doPad && !leftAlign) {
                formatter.out().append(padString);
            }
            formatter.out().append(uppercase ? representation.toUpperCase() : representation);
            if (doPad && leftAlign) {
                formatter.out().append(padString);
            }
        }
        catch (IOException e) {
            ExceptionLogger.getConsumer(new Class[0]).accept(e);
        }
    }
}

