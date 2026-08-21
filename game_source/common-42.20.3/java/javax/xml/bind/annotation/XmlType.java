/*
 * Decompiled with CFR 0.152.
 */
package javax.xml.bind.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.TYPE})
public @interface XmlType {
    public String name() default "##default";

    public String[] propOrder() default {""};

    public String namespace() default "##default";

    public Class factoryClass() default DEFAULT.class;

    public String factoryMethod() default "";

    public static final class DEFAULT {
    }
}

