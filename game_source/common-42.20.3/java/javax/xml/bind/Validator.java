/*
 * Decompiled with CFR 0.152.
 */
package javax.xml.bind;

import javax.xml.bind.JAXBException;
import javax.xml.bind.PropertyException;
import javax.xml.bind.ValidationEventHandler;

public interface Validator {
    public void setEventHandler(ValidationEventHandler var1) throws JAXBException;

    public ValidationEventHandler getEventHandler() throws JAXBException;

    public boolean validate(Object var1) throws JAXBException;

    public boolean validateRoot(Object var1) throws JAXBException;

    public void setProperty(String var1, Object var2) throws PropertyException;

    public Object getProperty(String var1) throws PropertyException;
}

