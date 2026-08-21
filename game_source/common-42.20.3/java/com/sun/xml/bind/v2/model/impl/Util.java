/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.impl;

import com.sun.xml.bind.v2.model.annotation.AnnotationReader;
import com.sun.xml.bind.v2.model.annotation.AnnotationSource;
import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.impl.Messages;
import com.sun.xml.bind.v2.model.impl.ModelBuilder;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationException;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSchemaTypes;
import javax.xml.namespace.QName;

final class Util {
    Util() {
    }

    static <T, C, F, M> QName calcSchemaType(AnnotationReader<T, C, F, M> reader, AnnotationSource primarySource, C enclosingClass, T individualType, Locatable src) {
        XmlSchemaType xst = primarySource.readAnnotation(XmlSchemaType.class);
        if (xst != null) {
            return new QName(xst.namespace(), xst.name());
        }
        XmlSchemaTypes xsts = reader.getPackageAnnotation(XmlSchemaTypes.class, enclosingClass, src);
        XmlSchemaType[] values2 = null;
        if (xsts != null) {
            values2 = xsts.value();
        } else {
            xst = reader.getPackageAnnotation(XmlSchemaType.class, enclosingClass, src);
            if (xst != null) {
                values2 = new XmlSchemaType[]{xst};
            }
        }
        if (values2 != null) {
            for (XmlSchemaType item : values2) {
                if (!reader.getClassValue(item, "type").equals(individualType)) continue;
                return new QName(item.namespace(), item.name());
            }
        }
        return null;
    }

    static MimeType calcExpectedMediaType(AnnotationSource primarySource, ModelBuilder builder) {
        XmlMimeType xmt = primarySource.readAnnotation(XmlMimeType.class);
        if (xmt == null) {
            return null;
        }
        try {
            return new MimeType(xmt.value());
        }
        catch (MimeTypeParseException e) {
            builder.reportError(new IllegalAnnotationException(Messages.ILLEGAL_MIME_TYPE.format(xmt.value(), e.getMessage()), xmt));
            return null;
        }
    }
}

