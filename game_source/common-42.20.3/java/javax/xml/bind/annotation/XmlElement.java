/*
 * Decompiled with CFR 0.152.
 */
package javax.xml.bind.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
public @interface XmlElement {
    public String name() default "##default";

    public boolean nillable() default false;

    public boolean required() default false;

    public String namespace() default "##default";

    public String defaultValue() default "\u0000";

    public Class type() default DEFAULT.class;

    public static final class DEFAULT {
    }
}

