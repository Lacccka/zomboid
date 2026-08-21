/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.output;

import com.sun.xml.bind.marshaller.SAX2DOMEx;
import com.sun.xml.bind.v2.runtime.AssociationMap;
import com.sun.xml.bind.v2.runtime.output.SAXOutput;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public final class DOMOutput
extends SAXOutput {
    private final AssociationMap assoc;

    public DOMOutput(Node node, AssociationMap assoc) {
        super(new SAX2DOMEx(node));
        this.assoc = assoc;
        assert (assoc != null);
    }

    private SAX2DOMEx getBuilder() {
        return (SAX2DOMEx)this.out;
    }

    @Override
    public void endStartTag() throws SAXException {
        Object ip;
        super.endStartTag();
        Object op = this.nsContext.getCurrent().getOuterPeer();
        if (op != null) {
            this.assoc.addOuter(this.getBuilder().getCurrentElement(), op);
        }
        if ((ip = this.nsContext.getCurrent().getInnerPeer()) != null) {
            this.assoc.addInner(this.getBuilder().getCurrentElement(), ip);
        }
    }
}

