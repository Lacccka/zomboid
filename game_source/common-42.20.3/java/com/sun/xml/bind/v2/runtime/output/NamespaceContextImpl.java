/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.output;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.NamespaceContext2;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.output.XmlOutput;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.SAXException;

public final class NamespaceContextImpl
implements NamespaceContext2 {
    private final XMLSerializer owner;
    private String[] prefixes = new String[4];
    private String[] nsUris = new String[4];
    private int size;
    private Element current;
    private final Element top;
    private NamespacePrefixMapper prefixMapper = defaultNamespacePrefixMapper;
    public boolean collectionMode;
    private static final NamespacePrefixMapper defaultNamespacePrefixMapper = new NamespacePrefixMapper(){

        @Override
        public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
            if (namespaceUri.equals("http://www.w3.org/2001/XMLSchema-instance")) {
                return "xsi";
            }
            if (namespaceUri.equals("http://www.w3.org/2001/XMLSchema")) {
                return "xs";
            }
            if (namespaceUri.equals("http://www.w3.org/2005/05/xmlmime")) {
                return "xmime";
            }
            return suggestion;
        }
    };

    public NamespaceContextImpl(XMLSerializer owner) {
        this.owner = owner;
        this.current = this.top = new Element(this, null);
        this.put("http://www.w3.org/XML/1998/namespace", "xml");
    }

    public void setPrefixMapper(NamespacePrefixMapper mapper) {
        if (mapper == null) {
            mapper = defaultNamespacePrefixMapper;
        }
        this.prefixMapper = mapper;
    }

    public NamespacePrefixMapper getPrefixMapper() {
        return this.prefixMapper;
    }

    public void reset() {
        this.current = this.top;
        this.size = 1;
        this.collectionMode = false;
    }

    public int declareNsUri(String uri, String preferedPrefix, boolean requirePrefix) {
        preferedPrefix = this.prefixMapper.getPreferredPrefix(uri, preferedPrefix, requirePrefix);
        if (uri.length() == 0) {
            for (int i = this.size - 1; i >= 0; --i) {
                if (this.nsUris[i].length() == 0) {
                    return i;
                }
                if (this.prefixes[i].length() != 0) continue;
                assert (this.current.defaultPrefixIndex == -1 && this.current.oldDefaultNamespaceUriIndex == -1);
                String oldUri = this.nsUris[i];
                String[] knownURIs = this.owner.nameList.namespaceURIs;
                if (this.current.baseIndex <= i) {
                    this.nsUris[i] = "";
                    int subst = this.put(oldUri, null);
                    for (int j = knownURIs.length - 1; j >= 0; --j) {
                        if (!knownURIs[j].equals(oldUri)) continue;
                        this.owner.knownUri2prefixIndexMap[j] = subst;
                        break;
                    }
                    if (this.current.elementLocalName != null) {
                        this.current.setTagName(subst, this.current.elementLocalName, this.current.getOuterPeer());
                    }
                    return i;
                }
                for (int j = knownURIs.length - 1; j >= 0; --j) {
                    if (!knownURIs[j].equals(oldUri)) continue;
                    this.current.defaultPrefixIndex = i;
                    this.current.oldDefaultNamespaceUriIndex = j;
                    this.owner.knownUri2prefixIndexMap[j] = this.size;
                    break;
                }
                if (this.current.elementLocalName != null) {
                    this.current.setTagName(this.size, this.current.elementLocalName, this.current.getOuterPeer());
                }
                this.put(this.nsUris[i], null);
                return this.put("", "");
            }
            return this.put("", "");
        }
        for (int i = this.size - 1; i >= 0; --i) {
            String p = this.prefixes[i];
            if (this.nsUris[i].equals(uri) && (!requirePrefix || p.length() > 0)) {
                return i;
            }
            if (!p.equals(preferedPrefix)) continue;
            preferedPrefix = null;
        }
        if (preferedPrefix == null && requirePrefix) {
            preferedPrefix = this.makeUniquePrefix();
        }
        return this.put(uri, preferedPrefix);
    }

    @Override
    public int force(@NotNull String uri, @NotNull String prefix) {
        for (int i = this.size - 1; i >= 0; --i) {
            if (!this.prefixes[i].equals(prefix)) continue;
            if (!this.nsUris[i].equals(uri)) break;
            return i;
        }
        return this.put(uri, prefix);
    }

    public int put(@NotNull String uri, @Nullable String prefix) {
        if (this.size == this.nsUris.length) {
            String[] u = new String[this.nsUris.length * 2];
            String[] p = new String[this.prefixes.length * 2];
            System.arraycopy(this.nsUris, 0, u, 0, this.nsUris.length);
            System.arraycopy(this.prefixes, 0, p, 0, this.prefixes.length);
            this.nsUris = u;
            this.prefixes = p;
        }
        if (prefix == null) {
            prefix = this.size == 1 ? "" : this.makeUniquePrefix();
        }
        this.nsUris[this.size] = uri;
        this.prefixes[this.size] = prefix;
        return this.size++;
    }

    private String makeUniquePrefix() {
        String prefix = new StringBuilder(5).append("ns").append(this.size).toString();
        while (this.getNamespaceURI(prefix) != null) {
            prefix = prefix + '_';
        }
        return prefix;
    }

    public Element getCurrent() {
        return this.current;
    }

    public int getPrefixIndex(String uri) {
        for (int i = this.size - 1; i >= 0; --i) {
            if (!this.nsUris[i].equals(uri)) continue;
            return i;
        }
        throw new IllegalStateException();
    }

    public String getPrefix(int prefixIndex) {
        return this.prefixes[prefixIndex];
    }

    public String getNamespaceURI(int prefixIndex) {
        return this.nsUris[prefixIndex];
    }

    @Override
    public String getNamespaceURI(String prefix) {
        for (int i = this.size - 1; i >= 0; --i) {
            if (!this.prefixes[i].equals(prefix)) continue;
            return this.nsUris[i];
        }
        return null;
    }

    @Override
    public String getPrefix(String uri) {
        if (this.collectionMode) {
            return this.declareNamespace(uri, null, false);
        }
        for (int i = this.size - 1; i >= 0; --i) {
            if (!this.nsUris[i].equals(uri)) continue;
            return this.prefixes[i];
        }
        return null;
    }

    @Override
    public Iterator<String> getPrefixes(String uri) {
        String prefix = this.getPrefix(uri);
        if (prefix == null) {
            return Collections.emptySet().iterator();
        }
        return Collections.singleton(uri).iterator();
    }

    @Override
    public String declareNamespace(String namespaceUri, String preferedPrefix, boolean requirePrefix) {
        int idx = this.declareNsUri(namespaceUri, preferedPrefix, requirePrefix);
        return this.getPrefix(idx);
    }

    public int count() {
        return this.size;
    }

    public final class Element {
        public final NamespaceContextImpl context;
        private final Element prev;
        private Element next;
        private int oldDefaultNamespaceUriIndex;
        private int defaultPrefixIndex;
        private int baseIndex;
        private final int depth;
        private int elementNamePrefix;
        private String elementLocalName;
        private Name elementName;
        private Object outerPeer;
        private Object innerPeer;

        private Element(NamespaceContextImpl context, Element prev) {
            this.context = context;
            this.prev = prev;
            this.depth = prev == null ? 0 : prev.depth + 1;
        }

        public boolean isRootElement() {
            return this.depth == 1;
        }

        public Element push() {
            if (this.next == null) {
                this.next = new Element(this.context, this);
            }
            this.next.onPushed();
            return this.next;
        }

        public Element pop() {
            if (this.oldDefaultNamespaceUriIndex >= 0) {
                ((NamespaceContextImpl)this.context).owner.knownUri2prefixIndexMap[this.oldDefaultNamespaceUriIndex] = this.defaultPrefixIndex;
            }
            this.context.size = this.baseIndex;
            this.context.current = this.prev;
            this.innerPeer = null;
            this.outerPeer = null;
            return this.prev;
        }

        private void onPushed() {
            this.defaultPrefixIndex = -1;
            this.oldDefaultNamespaceUriIndex = -1;
            this.baseIndex = this.context.size;
            this.context.current = this;
        }

        public void setTagName(int prefix, String localName, Object outerPeer) {
            assert (localName != null);
            this.elementNamePrefix = prefix;
            this.elementLocalName = localName;
            this.elementName = null;
            this.outerPeer = outerPeer;
        }

        public void setTagName(Name tagName, Object outerPeer) {
            assert (tagName != null);
            this.elementName = tagName;
            this.outerPeer = outerPeer;
        }

        public void startElement(XmlOutput out, Object innerPeer) throws IOException, XMLStreamException {
            this.innerPeer = innerPeer;
            if (this.elementName != null) {
                out.beginStartTag(this.elementName);
            } else {
                out.beginStartTag(this.elementNamePrefix, this.elementLocalName);
            }
        }

        public void endElement(XmlOutput out) throws IOException, SAXException, XMLStreamException {
            if (this.elementName != null) {
                out.endTag(this.elementName);
                this.elementName = null;
            } else {
                out.endTag(this.elementNamePrefix, this.elementLocalName);
            }
        }

        public final int count() {
            return this.context.size - this.baseIndex;
        }

        public final String getPrefix(int idx) {
            return this.context.prefixes[this.baseIndex + idx];
        }

        public final String getNsUri(int idx) {
            return this.context.nsUris[this.baseIndex + idx];
        }

        public int getBase() {
            return this.baseIndex;
        }

        public Object getOuterPeer() {
            return this.outerPeer;
        }

        public Object getInnerPeer() {
            return this.innerPeer;
        }

        public Element getParent() {
            return this.prev;
        }
    }
}

