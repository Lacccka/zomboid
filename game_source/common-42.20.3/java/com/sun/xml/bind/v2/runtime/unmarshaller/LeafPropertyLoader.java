/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.unmarshaller;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor;
import com.sun.xml.bind.v2.runtime.unmarshaller.Loader;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import org.xml.sax.SAXException;

public class LeafPropertyLoader
extends Loader {
    private final TransducedAccessor xacc;

    public LeafPropertyLoader(TransducedAccessor xacc) {
        super(true);
        this.xacc = xacc;
    }

    @Override
    public void text(UnmarshallingContext.State state, CharSequence text) throws SAXException {
        try {
            this.xacc.parse(state.getPrev().getTarget(), text);
        }
        catch (AccessorException e) {
            LeafPropertyLoader.handleGenericException(e, true);
        }
        catch (RuntimeException e) {
            LeafPropertyLoader.handleParseConversionException(state, e);
        }
    }
}

