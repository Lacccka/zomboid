/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.core;

import com.sun.istack.Nullable;
import com.sun.xml.bind.v2.model.annotation.AnnotationSource;
import com.sun.xml.bind.v2.model.core.Adapter;
import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.core.TypeInfo;
import java.util.Collection;
import javax.activation.MimeType;
import javax.xml.namespace.QName;

public interface PropertyInfo<T, C>
extends AnnotationSource {
    public TypeInfo<T, C> parent();

    public String getName();

    public String displayName();

    public boolean isCollection();

    public Collection<? extends TypeInfo<T, C>> ref();

    public PropertyKind kind();

    public Adapter<T, C> getAdapter();

    public ID id();

    public MimeType getExpectedMimeType();

    public boolean inlineBinaryData();

    @Nullable
    public QName getSchemaType();
}

