/*
 * Decompiled with CFR 0.152.
 */
package javax.xml.bind.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.METHOD})
public @interface XmlElementDecl {
    public Class scope() default GLOBAL.class;

    public String namespace() default "##default";

    public String name();

    public String substitutionHeadNamespace() default "##default";

    public String substitutionHeadName() default "";

    public String defaultValue() default "\u0000";

    public static final class GLOBAL {
    }
}

