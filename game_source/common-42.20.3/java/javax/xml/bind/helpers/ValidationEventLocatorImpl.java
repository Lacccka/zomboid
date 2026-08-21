/*
 * Decompiled with CFR 0.152.
 */
package javax.xml.bind.helpers;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import javax.xml.bind.ValidationEventLocator;
import javax.xml.bind.helpers.Messages;
import org.w3c.dom.Node;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

public class ValidationEventLocatorImpl
implements ValidationEventLocator {
    private URL url = null;
    private int offset = -1;
    private int lineNumber = -1;
    private int columnNumber = -1;
    private Object object = null;
    private Node node = null;

    public ValidationEventLocatorImpl() {
    }

    public ValidationEventLocatorImpl(Locator loc) {
        if (loc == null) {
            throw new IllegalArgumentException(Messages.format("Shared.MustNotBeNull", "loc"));
        }
        this.url = ValidationEventLocatorImpl.toURL(loc.getSystemId());
        this.columnNumber = loc.getColumnNumber();
        this.lineNumber = loc.getLineNumber();
    }

    public ValidationEventLocatorImpl(SAXParseException e) {
        if (e == null) {
            throw new IllegalArgumentException(Messages.format("Shared.MustNotBeNull", "e"));
        }
        this.url = ValidationEventLocatorImpl.toURL(e.getSystemId());
        this.columnNumber = e.getColumnNumber();
        this.lineNumber = e.getLineNumber();
    }

    public ValidationEventLocatorImpl(Node _node) {
        if (_node == null) {
            throw new IllegalArgumentException(Messages.format("Shared.MustNotBeNull", "_node"));
        }
        this.node = _node;
    }

    public ValidationEventLocatorImpl(Object _object) {
        if (_object == null) {
            throw new IllegalArgumentException(Messages.format("Shared.MustNotBeNull", "_object"));
        }
        this.object = _object;
    }

    private static URL toURL(String systemId) {
        try {
            return new URL(systemId);
        }
        catch (MalformedURLException e) {
            return null;
        }
    }

    @Override
    public URL getURL() {
        return this.url;
    }

    public void setURL(URL _url) {
        this.url = _url;
    }

    @Override
    public int getOffset() {
        return this.offset;
    }

    public void setOffset(int _offset) {
        this.offset = _offset;
    }

    @Override
    public int getLineNumber() {
        return this.lineNumber;
    }

    public void setLineNumber(int _lineNumber) {
        this.lineNumber = _lineNumber;
    }

    @Override
    public int getColumnNumber() {
        return this.columnNumber;
    }

    public void setColumnNumber(int _columnNumber) {
        this.columnNumber = _columnNumber;
    }

    @Override
    public Object getObject() {
        return this.object;
    }

    public void setObject(Object _object) {
        this.object = _object;
    }

    @Override
    public Node getNode() {
        return this.node;
    }

    public void setNode(Node _node) {
        this.node = _node;
    }

    public String toString() {
        return MessageFormat.format("[node={0},object={1},url={2},line={3},col={4},offset={5}]", this.getNode(), this.getObject(), this.getURL(), String.valueOf(this.getLineNumber()), String.valueOf(this.getColumnNumber()), String.valueOf(this.getOffset()));
    }
}

