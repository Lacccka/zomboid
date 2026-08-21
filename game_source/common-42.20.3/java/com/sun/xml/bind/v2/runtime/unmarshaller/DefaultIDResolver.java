/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.unmarshaller;

import com.sun.xml.bind.IDResolver;
import java.util.HashMap;
import java.util.concurrent.Callable;
import javax.xml.bind.ValidationEventHandler;
import org.xml.sax.SAXException;

final class DefaultIDResolver
extends IDResolver {
    private HashMap<String, Object> idmap = null;

    DefaultIDResolver() {
    }

    @Override
    public void startDocument(ValidationEventHandler eventHandler) throws SAXException {
        if (this.idmap != null) {
            this.idmap.clear();
        }
    }

    @Override
    public void bind(String id, Object obj) {
        if (this.idmap == null) {
            this.idmap = new HashMap();
        }
        this.idmap.put(id, obj);
    }

    public Callable resolve(final String id, Class targetType) {
        return new Callable(){

            public Object call() throws Exception {
                if (DefaultIDResolver.this.idmap == null) {
                    return null;
                }
                return DefaultIDResolver.this.idmap.get(id);
            }
        };
    }
}

