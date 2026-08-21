/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.annotation;

import com.sun.xml.bind.v2.model.annotation.Quick;
import com.sun.xml.bind.v2.model.annotation.XmlAttributeQuick;
import com.sun.xml.bind.v2.model.annotation.XmlElementDeclQuick;
import com.sun.xml.bind.v2.model.annotation.XmlElementQuick;
import com.sun.xml.bind.v2.model.annotation.XmlElementRefQuick;
import com.sun.xml.bind.v2.model.annotation.XmlElementRefsQuick;
import com.sun.xml.bind.v2.model.annotation.XmlEnumQuick;
import com.sun.xml.bind.v2.model.annotation.XmlRootElementQuick;
import com.sun.xml.bind.v2.model.annotation.XmlSchemaQuick;
import com.sun.xml.bind.v2.model.annotation.XmlSchemaTypeQuick;
import com.sun.xml.bind.v2.model.annotation.XmlTransientQuick;
import com.sun.xml.bind.v2.model.annotation.XmlTypeQuick;
import com.sun.xml.bind.v2.model.annotation.XmlValueQuick;

class Init {
    Init() {
    }

    static Quick[] getAll() {
        return new Quick[]{new XmlAttributeQuick(null, null), new XmlElementQuick(null, null), new XmlElementDeclQuick(null, null), new XmlElementRefQuick(null, null), new XmlElementRefsQuick(null, null), new XmlEnumQuick(null, null), new XmlRootElementQuick(null, null), new XmlSchemaQuick(null, null), new XmlSchemaTypeQuick(null, null), new XmlTransientQuick(null, null), new XmlTypeQuick(null, null), new XmlValueQuick(null, null)};
    }
}

