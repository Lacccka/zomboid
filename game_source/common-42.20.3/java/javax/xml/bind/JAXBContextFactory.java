/*
 * Decompiled with CFR 0.152.
 */
package javax.xml.bind;

import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

public interface JAXBContextFactory {
    public JAXBContext createContext(Class<?>[] var1, Map<String, ?> var2) throws JAXBException;

    public JAXBContext createContext(String var1, ClassLoader var2, Map<String, ?> var3) throws JAXBException;
}

