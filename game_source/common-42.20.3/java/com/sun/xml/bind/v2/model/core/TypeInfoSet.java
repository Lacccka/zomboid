/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.core;

import com.sun.xml.bind.v2.model.core.ArrayInfo;
import com.sun.xml.bind.v2.model.core.BuiltinLeafInfo;
import com.sun.xml.bind.v2.model.core.ClassInfo;
import com.sun.xml.bind.v2.model.core.ElementInfo;
import com.sun.xml.bind.v2.model.core.EnumLeafInfo;
import com.sun.xml.bind.v2.model.core.NonElement;
import com.sun.xml.bind.v2.model.core.Ref;
import com.sun.xml.bind.v2.model.nav.Navigator;
import java.util.Map;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;

public interface TypeInfoSet<T, C, F, M> {
    public Navigator<T, C, F, M> getNavigator();

    public NonElement<T, C> getTypeInfo(T var1);

    public NonElement<T, C> getAnyTypeInfo();

    public NonElement<T, C> getClassInfo(C var1);

    public Map<? extends T, ? extends ArrayInfo<T, C>> arrays();

    public Map<C, ? extends ClassInfo<T, C>> beans();

    public Map<T, ? extends BuiltinLeafInfo<T, C>> builtins();

    public Map<C, ? extends EnumLeafInfo<T, C>> enums();

    public ElementInfo<T, C> getElementInfo(C var1, QName var2);

    public NonElement<T, C> getTypeInfo(Ref<T, C> var1);

    public Map<QName, ? extends ElementInfo<T, C>> getElementMappings(C var1);

    public Iterable<? extends ElementInfo<T, C>> getAllElements();

    public Map<String, String> getXmlNs(String var1);

    public Map<String, String> getSchemaLocations();

    public XmlNsForm getElementFormDefault(String var1);

    public XmlNsForm getAttributeFormDefault(String var1);

    public void dump(Result var1) throws JAXBException;
}

