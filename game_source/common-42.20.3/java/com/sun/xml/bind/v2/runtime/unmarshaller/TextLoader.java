/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.unmarshaller;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.runtime.Transducer;
import com.sun.xml.bind.v2.runtime.unmarshaller.Loader;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import org.xml.sax.SAXException;

public class TextLoader
extends Loader {
    private final Transducer xducer;

    public TextLoader(Transducer xducer) {
        super(true);
        this.xducer = xducer;
    }

    @Override
    public void text(UnmarshallingContext.State state, CharSequence text) throws SAXException {
        try {
            state.setTarget(this.xducer.parse(text));
        }
        catch (AccessorException e) {
            TextLoader.handleGenericException(e, true);
        }
        catch (RuntimeException e) {
            TextLoader.handleParseConversionException(state, e);
        }
    }
}

