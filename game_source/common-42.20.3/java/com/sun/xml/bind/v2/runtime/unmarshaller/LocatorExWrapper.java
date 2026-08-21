/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.unmarshaller;

import com.sun.xml.bind.v2.runtime.unmarshaller.LocatorEx;
import javax.xml.bind.ValidationEventLocator;
import javax.xml.bind.helpers.ValidationEventLocatorImpl;
import org.xml.sax.Locator;

class LocatorExWrapper
implements LocatorEx {
    private final Locator locator;

    public LocatorExWrapper(Locator locator) {
        this.locator = locator;
    }

    @Override
    public ValidationEventLocator getLocation() {
        return new ValidationEventLocatorImpl(this.locator);
    }

    @Override
    public String getPublicId() {
        return this.locator.getPublicId();
    }

    @Override
    public String getSystemId() {
        return this.locator.getSystemId();
    }

    @Override
    public int getLineNumber() {
        return this.locator.getLineNumber();
    }

    @Override
    public int getColumnNumber() {
        return this.locator.getColumnNumber();
    }
}

