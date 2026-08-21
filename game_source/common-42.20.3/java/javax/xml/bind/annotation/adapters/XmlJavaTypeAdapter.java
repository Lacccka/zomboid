/*
 * Decompiled with CFR 0.152.
 */
package javax.xml.bind.annotation.adapters;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.xml.bind.annotation.adapters.XmlAdapter;

@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.PACKAGE, ElementType.FIELD, ElementType.METHOD, ElementType.TYPE, ElementType.PARAMETER})
public @interface XmlJavaTypeAdapter {
    public Class<? extends XmlAdapter> value();

    public Class type() default DEFAULT.class;

    public static final class DEFAULT {
    }
}

