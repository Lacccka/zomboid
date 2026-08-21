/*
 * Decompiled with CFR 0.152.
 */
package javax.xml.bind.util;

import java.text.MessageFormat;
import java.util.ResourceBundle;

class Messages {
    static final String UNRECOGNIZED_SEVERITY = "ValidationEventCollector.UnrecognizedSeverity";
    static final String RESULT_NULL_CONTEXT = "JAXBResult.NullContext";
    static final String RESULT_NULL_UNMARSHALLER = "JAXBResult.NullUnmarshaller";
    static final String SOURCE_NULL_CONTEXT = "JAXBSource.NullContext";
    static final String SOURCE_NULL_CONTENT = "JAXBSource.NullContent";
    static final String SOURCE_NULL_MARSHALLER = "JAXBSource.NullMarshaller";

    Messages() {
    }

    static String format(String property) {
        return Messages.format(property, null);
    }

    static String format(String property, Object arg1) {
        return Messages.format(property, new Object[]{arg1});
    }

    static String format(String property, Object arg1, Object arg2) {
        return Messages.format(property, new Object[]{arg1, arg2});
    }

    static String format(String property, Object arg1, Object arg2, Object arg3) {
        return Messages.format(property, new Object[]{arg1, arg2, arg3});
    }

    static String format(String property, Object[] args2) {
        String text = ResourceBundle.getBundle(Messages.class.getName()).getString(property);
        return MessageFormat.format(text, args2);
    }
}

