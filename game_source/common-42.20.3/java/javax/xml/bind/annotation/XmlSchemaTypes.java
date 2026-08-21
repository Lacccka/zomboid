/*
 * Decompiled with CFR 0.152.
 */
package javax.xml.bind.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.xml.bind.annotation.XmlSchemaType;

@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.PACKAGE})
public @interface XmlSchemaTypes {
    public XmlSchemaType[] value();
}

