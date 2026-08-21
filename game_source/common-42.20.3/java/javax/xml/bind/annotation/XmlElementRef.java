/*
 * Decompiled with CFR 0.152.
 */
package javax.xml.bind.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.FIELD, ElementType.METHOD})
public @interface XmlElementRef {
    public Class type() default DEFAULT.class;

    public String namespace() default "";

    public String name() default "##default";

    public boolean required() default true;

    public static final class DEFAULT {
    }
}

