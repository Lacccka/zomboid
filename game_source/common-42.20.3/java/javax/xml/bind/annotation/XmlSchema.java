/*
 * Decompiled with CFR 0.152.
 */
package javax.xml.bind.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;

@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.PACKAGE})
public @interface XmlSchema {
    public static final String NO_LOCATION = "##generate";

    public XmlNs[] xmlns() default {};

    public String namespace() default "";

    public XmlNsForm elementFormDefault() default XmlNsForm.UNSET;

    public XmlNsForm attributeFormDefault() default XmlNsForm.UNSET;

    public String location() default "##generate";
}

