/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sun.xml.txw2.TypedXmlWriter
 */
package com.sun.xml.bind.v2.schemagen;

import com.sun.xml.bind.v2.schemagen.xmlschema.LocalAttribute;
import com.sun.xml.bind.v2.schemagen.xmlschema.LocalElement;
import com.sun.xml.bind.v2.schemagen.xmlschema.Schema;
import com.sun.xml.txw2.TypedXmlWriter;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.namespace.QName;

enum Form {
    QUALIFIED(XmlNsForm.QUALIFIED, true){

        @Override
        void declare(String attName, Schema schema) {
            schema._attribute(attName, "qualified");
        }
    }
    ,
    UNQUALIFIED(XmlNsForm.UNQUALIFIED, false){

        @Override
        void declare(String attName, Schema schema) {
            schema._attribute(attName, "unqualified");
        }
    }
    ,
    UNSET(XmlNsForm.UNSET, false){

        @Override
        void declare(String attName, Schema schema) {
        }
    };

    private final XmlNsForm xnf;
    public final boolean isEffectivelyQualified;

    private Form(XmlNsForm xnf, boolean effectivelyQualified) {
        this.xnf = xnf;
        this.isEffectivelyQualified = effectivelyQualified;
    }

    abstract void declare(String var1, Schema var2);

    public void writeForm(LocalElement e, QName tagName) {
        this._writeForm(e, tagName);
    }

    public void writeForm(LocalAttribute a, QName tagName) {
        this._writeForm(a, tagName);
    }

    private void _writeForm(TypedXmlWriter e, QName tagName) {
        boolean qualified;
        boolean bl = qualified = tagName.getNamespaceURI().length() > 0;
        if (qualified && this != QUALIFIED) {
            e._attribute("form", (Object)"qualified");
        } else if (!qualified && this == QUALIFIED) {
            e._attribute("form", (Object)"unqualified");
        }
    }

    public static Form get(XmlNsForm xnf) {
        for (Form v : Form.values()) {
            if (v.xnf != xnf) continue;
            return v;
        }
        throw new IllegalArgumentException();
    }
}

