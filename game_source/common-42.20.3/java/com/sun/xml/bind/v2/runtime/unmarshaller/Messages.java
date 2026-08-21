/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.unmarshaller;

import java.text.MessageFormat;
import java.util.ResourceBundle;

enum Messages {
    UNRESOLVED_IDREF,
    UNEXPECTED_ELEMENT,
    UNEXPECTED_TEXT,
    NOT_A_QNAME,
    UNRECOGNIZED_TYPE_NAME,
    UNRECOGNIZED_TYPE_NAME_MAYBE,
    UNABLE_TO_CREATE_MAP,
    UNINTERNED_STRINGS,
    ERRORS_LIMIT_EXCEEDED;

    private static final ResourceBundle rb;

    public String toString() {
        return this.format(new Object[0]);
    }

    public String format(Object ... args2) {
        return MessageFormat.format(rb.getString(this.name()), args2);
    }

    static {
        rb = ResourceBundle.getBundle(Messages.class.getName());
    }
}

