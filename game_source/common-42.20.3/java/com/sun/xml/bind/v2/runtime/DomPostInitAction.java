/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime;

import com.sun.xml.bind.v2.runtime.XMLSerializer;
import java.util.HashSet;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

final class DomPostInitAction
implements Runnable {
    private final Node node;
    private final XMLSerializer serializer;

    DomPostInitAction(Node node, XMLSerializer serializer) {
        this.node = node;
        this.serializer = serializer;
    }

    @Override
    public void run() {
        HashSet<String> declaredPrefixes = new HashSet<String>();
        for (Node n = this.node; n != null && n.getNodeType() == 1; n = n.getParentNode()) {
            NamedNodeMap atts = n.getAttributes();
            if (atts == null) continue;
            for (int i = 0; i < atts.getLength(); ++i) {
                String value;
                String prefix;
                Attr a = (Attr)atts.item(i);
                String nsUri = a.getNamespaceURI();
                if (nsUri == null || !nsUri.equals("http://www.w3.org/2000/xmlns/") || (prefix = a.getLocalName()) == null) continue;
                if (prefix.equals("xmlns")) {
                    prefix = "";
                }
                if ((value = a.getValue()) == null || !declaredPrefixes.add(prefix)) continue;
                this.serializer.addInscopeBinding(value, prefix);
            }
        }
    }
}

