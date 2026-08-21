/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system.ffm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.foreign.ValueLayout;
import java.nio.ByteOrder;
import org.lwjgl.system.Checks;

@Target(value={ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface FFMCharset {
    public static final Type DEFAULT = Type.ISO_8859_1;

    public Type value() default Type.ISO_8859_1;

    public static enum Type {
        US_ASCII(ValueLayout.JAVA_BYTE, "US_ASCII"),
        ISO_8859_1(ValueLayout.JAVA_BYTE, "ISO_8859_1"),
        UTF8(ValueLayout.JAVA_BYTE, "UTF_8"),
        UTF16(Checks.DEBUG ? ValueLayout.JAVA_SHORT : ValueLayout.JAVA_SHORT_UNALIGNED, ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN ? "UTF_16LE" : "UTF_16BE"),
        UTF32(Checks.DEBUG ? ValueLayout.JAVA_INT : ValueLayout.JAVA_INT_UNALIGNED, ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN ? "UTF_32LE" : "UTF_32BE");

        public final ValueLayout layout;
        public final String charset;

        private Type(ValueLayout layout, String charset) {
            this.layout = layout;
            this.charset = charset;
        }
    }
}

