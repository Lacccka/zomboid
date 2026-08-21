/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.marshaller;

import com.sun.istack.FinalArrayList;
import com.sun.xml.bind.marshaller.Messages;
import com.sun.xml.bind.util.Which;
import com.sun.xml.bind.v2.util.XmlFactory;
import java.util.Stack;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class SAX2DOMEx
implements ContentHandler {
    private Node node = null;
    private boolean isConsolidate;
    protected final Stack<Node> nodeStack = new Stack();
    private final FinalArrayList<String> unprocessedNamespaces = new FinalArrayList();
    protected final Document document;

    public SAX2DOMEx(Node node) {
        this(node, false);
    }

    public SAX2DOMEx(Node node, boolean isConsolidate) {
        this.node = node;
        this.isConsolidate = isConsolidate;
        this.nodeStack.push(this.node);
        this.document = node instanceof Document ? (Document)node : node.getOwnerDocument();
    }

    public SAX2DOMEx(DocumentBuilderFactory f) throws ParserConfigurationException {
        f.setValidating(false);
        this.document = f.newDocumentBuilder().newDocument();
        this.node = this.document;
        this.nodeStack.push(this.document);
    }

    public SAX2DOMEx() throws ParserConfigurationException {
        DocumentBuilderFactory factory2 = XmlFactory.createDocumentBuilderFactory(false);
        factory2.setValidating(false);
        this.document = factory2.newDocumentBuilder().newDocument();
        this.node = this.document;
        this.nodeStack.push(this.document);
    }

    public final Element getCurrentElement() {
        return (Element)this.nodeStack.peek();
    }

    public Node getDOM() {
        return this.node;
    }

    @Override
    public void startDocument() {
    }

    @Override
    public void endDocument() {
    }

    protected void namespace(Element element, String prefix, String uri) {
        String qname = "".equals(prefix) || prefix == null ? "xmlns" : "xmlns:" + prefix;
        if (element.hasAttributeNS("http://www.w3.org/2000/xmlns/", qname)) {
            element.removeAttributeNS("http://www.w3.org/2000/xmlns/", qname);
        }
        element.setAttributeNS("http://www.w3.org/2000/xmlns/", qname, uri);
    }

    @Override
    public void startElement(String namespace, String localName, String qName, Attributes attrs) {
        Node parent = this.nodeStack.peek();
        Element element = this.document.createElementNS(namespace, qName);
        if (element == null) {
            throw new AssertionError((Object)Messages.format("SAX2DOMEx.DomImplDoesntSupportCreateElementNs", this.document.getClass().getName(), Which.which(this.document.getClass())));
        }
        for (int i = 0; i < this.unprocessedNamespaces.size(); i += 2) {
            String prefix = (String)this.unprocessedNamespaces.get(i);
            String uri = (String)this.unprocessedNamespaces.get(i + 1);
            this.namespace(element, prefix, uri);
        }
        this.unprocessedNamespaces.clear();
        if (attrs != null) {
            int length = attrs.getLength();
            for (int i = 0; i < length; ++i) {
                String namespaceuri = attrs.getURI(i);
                String value = attrs.getValue(i);
                String qname = attrs.getQName(i);
                element.setAttributeNS(namespaceuri, qname, value);
            }
        }
        parent.appendChild(element);
        this.nodeStack.push(element);
    }

    @Override
    public void endElement(String namespace, String localName, String qName) {
        this.nodeStack.pop();
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        this.characters(new String(ch, start, length));
    }

    protected Text characters(String s) {
        Text text;
        Node parent = this.nodeStack.peek();
        Node lastChild = parent.getLastChild();
        if (this.isConsolidate && lastChild != null && lastChild.getNodeType() == 3) {
            text = (Text)lastChild;
            text.appendData(s);
        } else {
            text = this.document.createTextNode(s);
            parent.appendChild(text);
        }
        return text;
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) {
    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException {
        Node parent = this.nodeStack.peek();
        ProcessingInstruction n = this.document.createProcessingInstruction(target, data);
        parent.appendChild(n);
    }

    @Override
    public void setDocumentLocator(Locator locator) {
    }

    @Override
    public void skippedEntity(String name) {
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) {
        this.unprocessedNamespaces.add(prefix);
        this.unprocessedNamespaces.add(uri);
    }

    @Override
    public void endPrefixMapping(String prefix) {
    }
}

