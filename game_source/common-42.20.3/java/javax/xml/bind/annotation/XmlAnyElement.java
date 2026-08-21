/*
 * Decompiled with CFR 0.152.
 */
package javax.xml.bind.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.xml.bind.annotation.DomHandler;
import javax.xml.bind.annotation.W3CDomHandler;

@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.FIELD, ElementType.METHOD})
public @interface XmlAnyElement {
    public boolean lax() default false;

    public Class<? extends DomHandler> value() default W3CDomHandler.class;
}

