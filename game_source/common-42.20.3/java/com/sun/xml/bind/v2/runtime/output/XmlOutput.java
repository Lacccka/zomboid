/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.output;

import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.output.NamespaceContextImpl;
import com.sun.xml.bind.v2.runtime.output.Pcdata;
import java.io.IOException;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.SAXException;

public interface XmlOutput {
    public void startDocument(XMLSerializer var1, boolean var2, int[] var3, NamespaceContextImpl var4) throws IOException, SAXException, XMLStreamException;

    public void endDocument(boolean var1) throws IOException, SAXException, XMLStreamException;

    public void beginStartTag(Name var1) throws IOException, XMLStreamException;

    public void beginStartTag(int var1, String var2) throws IOException, XMLStreamException;

    public void attribute(Name var1, String var2) throws IOException, XMLStreamException;

    public void attribute(int var1, String var2, String var3) throws IOException, XMLStreamException;

    public void endStartTag() throws IOException, SAXException;

    public void endTag(Name var1) throws IOException, SAXException, XMLStreamException;

    public void endTag(int var1, String var2) throws IOException, SAXException, XMLStreamException;

    public void text(String var1, boolean var2) throws IOException, SAXException, XMLStreamException;

    public void text(Pcdata var1, boolean var2) throws IOException, SAXException, XMLStreamException;
}

