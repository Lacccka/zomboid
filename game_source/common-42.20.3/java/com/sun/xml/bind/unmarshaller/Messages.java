/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.unmarshaller;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public class Messages {
    public static final String UNEXPECTED_ENTER_ELEMENT = "ContentHandlerEx.UnexpectedEnterElement";
    public static final String UNEXPECTED_LEAVE_ELEMENT = "ContentHandlerEx.UnexpectedLeaveElement";
    public static final String UNEXPECTED_ENTER_ATTRIBUTE = "ContentHandlerEx.UnexpectedEnterAttribute";
    public static final String UNEXPECTED_LEAVE_ATTRIBUTE = "ContentHandlerEx.UnexpectedLeaveAttribute";
    public static final String UNEXPECTED_TEXT = "ContentHandlerEx.UnexpectedText";
    public static final String UNEXPECTED_LEAVE_CHILD = "ContentHandlerEx.UnexpectedLeaveChild";
    public static final String UNEXPECTED_ROOT_ELEMENT = "SAXUnmarshallerHandlerImpl.UnexpectedRootElement";
    public static final String UNDEFINED_PREFIX = "Util.UndefinedPrefix";
    public static final String NULL_READER = "Unmarshaller.NullReader";
    public static final String ILLEGAL_READER_STATE = "Unmarshaller.IllegalReaderState";

    public static String format(String property) {
        return Messages.format(property, null);
    }

    public static String format(String property, Object arg1) {
        return Messages.format(property, new Object[]{arg1});
    }

    public static String format(String property, Object arg1, Object arg2) {
        return Messages.format(property, new Object[]{arg1, arg2});
    }

    public static String format(String property, Object arg1, Object arg2, Object arg3) {
        return Messages.format(property, new Object[]{arg1, arg2, arg3});
    }

    public static String format(String property, Object[] args2) {
        String text = ResourceBundle.getBundle(Messages.class.getName()).getString(property);
        return MessageFormat.format(text, args2);
    }
}

