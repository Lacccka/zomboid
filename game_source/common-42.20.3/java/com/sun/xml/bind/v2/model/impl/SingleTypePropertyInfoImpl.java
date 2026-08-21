/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.impl;

import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.bind.v2.model.core.NonElement;
import com.sun.xml.bind.v2.model.core.PropertyInfo;
import com.sun.xml.bind.v2.model.impl.ClassInfoImpl;
import com.sun.xml.bind.v2.model.impl.Messages;
import com.sun.xml.bind.v2.model.impl.PropertyInfoImpl;
import com.sun.xml.bind.v2.model.impl.PropertySeed;
import com.sun.xml.bind.v2.model.impl.RuntimeBuiltinLeafInfoImpl;
import com.sun.xml.bind.v2.model.impl.RuntimeClassInfoImpl;
import com.sun.xml.bind.v2.model.impl.RuntimeModelBuilder;
import com.sun.xml.bind.v2.model.runtime.RuntimeNonElementRef;
import com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationException;
import com.sun.xml.bind.v2.runtime.Transducer;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlList;

abstract class SingleTypePropertyInfoImpl<T, C, F, M>
extends PropertyInfoImpl<T, C, F, M> {
    private NonElement<T, C> type;
    private final Accessor acc;
    private Transducer xducer;

    public SingleTypePropertyInfoImpl(ClassInfoImpl<T, C, F, M> classInfo, PropertySeed<T, C, F, M> seed) {
        super(classInfo, seed);
        if (this instanceof RuntimePropertyInfo) {
            Accessor rawAcc = ((RuntimeClassInfoImpl.RuntimePropertySeed)seed).getAccessor();
            if (this.getAdapter() != null && !this.isCollection()) {
                rawAcc = rawAcc.adapt(((RuntimePropertyInfo)((Object)this)).getAdapter());
            }
            this.acc = rawAcc;
        } else {
            this.acc = null;
        }
    }

    public List<? extends NonElement<T, C>> ref() {
        return Collections.singletonList(this.getTarget());
    }

    public NonElement<T, C> getTarget() {
        if (this.type == null) {
            assert (this.parent.builder != null) : "this method must be called during the build stage";
            this.type = this.parent.builder.getTypeInfo(this.getIndividualType(), this);
        }
        return this.type;
    }

    public PropertyInfo<T, C> getSource() {
        return this;
    }

    @Override
    public void link() {
        super.link();
        if (!NonElement.ANYTYPE_NAME.equals(this.type.getTypeName()) && !this.type.isSimpleType() && this.id() != ID.IDREF) {
            this.parent.builder.reportError(new IllegalAnnotationException(Messages.SIMPLE_TYPE_IS_REQUIRED.format(new Object[0]), this.seed));
        }
        if (!this.isCollection() && this.seed.hasAnnotation(XmlList.class)) {
            this.parent.builder.reportError(new IllegalAnnotationException(Messages.XMLLIST_ON_SINGLE_PROPERTY.format(new Object[0]), this));
        }
    }

    public Accessor getAccessor() {
        return this.acc;
    }

    public Transducer getTransducer() {
        if (this.xducer == null) {
            this.xducer = RuntimeModelBuilder.createTransducer((RuntimeNonElementRef)((Object)this));
            if (this.xducer == null) {
                this.xducer = RuntimeBuiltinLeafInfoImpl.STRING;
            }
        }
        return this.xducer;
    }
}

