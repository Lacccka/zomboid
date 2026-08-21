/*
 * Decompiled with CFR 0.152.
 */
package javax.xml.bind.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={})
public @interface XmlNs {
    public String prefix();

    public String namespaceURI();
}

