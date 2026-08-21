/*
 * Decompiled with CFR 0.152.
 */
package javax.xml.bind.annotation;

import javax.xml.bind.ValidationEventHandler;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

public interface DomHandler<ElementT, ResultT extends Result> {
    public ResultT createUnmarshaller(ValidationEventHandler var1);

    public ElementT getElement(ResultT var1);

    public Source marshal(ElementT var1, ValidationEventHandler var2);
}

