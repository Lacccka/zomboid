/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.runtime;

import com.sun.xml.bind.v2.model.core.ReferencePropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeElement;
import com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo;
import java.lang.reflect.Type;
import java.util.Set;

public interface RuntimeReferencePropertyInfo
extends ReferencePropertyInfo<Type, Class>,
RuntimePropertyInfo {
    @Override
    public Set<? extends RuntimeElement> getElements();
}

