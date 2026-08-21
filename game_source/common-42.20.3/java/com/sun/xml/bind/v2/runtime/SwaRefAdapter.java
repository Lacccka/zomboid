/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime;

import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import javax.activation.DataHandler;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.bind.attachment.AttachmentUnmarshaller;

public final class SwaRefAdapter
extends XmlAdapter<String, DataHandler> {
    @Override
    public DataHandler unmarshal(String cid) {
        AttachmentUnmarshaller au = UnmarshallingContext.getInstance().parent.getAttachmentUnmarshaller();
        return au.getAttachmentAsDataHandler(cid);
    }

    @Override
    public String marshal(DataHandler data) {
        if (data == null) {
            return null;
        }
        AttachmentMarshaller am = XMLSerializer.getInstance().attachmentMarshaller;
        return am.addSwaRefAttachment(data);
    }
}

