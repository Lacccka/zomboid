/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime;

import com.sun.xml.bind.api.BridgeContext;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.MarshallerImpl;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallerImpl;
import javax.xml.bind.JAXBException;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.bind.attachment.AttachmentUnmarshaller;

public final class BridgeContextImpl
extends BridgeContext {
    public final UnmarshallerImpl unmarshaller;
    public final MarshallerImpl marshaller;

    BridgeContextImpl(JAXBContextImpl context) {
        this.unmarshaller = context.createUnmarshaller();
        this.marshaller = context.createMarshaller();
    }

    @Override
    public void setErrorHandler(ValidationEventHandler handler) {
        try {
            this.unmarshaller.setEventHandler(handler);
            this.marshaller.setEventHandler(handler);
        }
        catch (JAXBException e) {
            throw new Error(e);
        }
    }

    @Override
    public void setAttachmentMarshaller(AttachmentMarshaller m) {
        this.marshaller.setAttachmentMarshaller(m);
    }

    @Override
    public void setAttachmentUnmarshaller(AttachmentUnmarshaller u) {
        this.unmarshaller.setAttachmentUnmarshaller(u);
    }

    @Override
    public AttachmentMarshaller getAttachmentMarshaller() {
        return this.marshaller.getAttachmentMarshaller();
    }

    @Override
    public AttachmentUnmarshaller getAttachmentUnmarshaller() {
        return this.unmarshaller.getAttachmentUnmarshaller();
    }
}

