/*
 * Decompiled with CFR 0.152.
 */
package javax.xml.bind.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.FIELD, ElementType.METHOD, ElementType.PACKAGE})
public @interface XmlSchemaType {
    public String name();

    public String namespace() default "http://www.w3.org/2001/XMLSchema";

    public Class type() default DEFAULT.class;

    public static final class DEFAULT {
    }
}

