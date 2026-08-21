/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.util;

import com.sun.xml.bind.v2.Messages;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

public class XmlFactory {
    public static final String ACCESS_EXTERNAL_SCHEMA = "http://javax.xml.XMLConstants/property/accessExternalSchema";
    public static final String ACCESS_EXTERNAL_DTD = "http://javax.xml.XMLConstants/property/accessExternalDTD";
    private static final Logger LOGGER = Logger.getLogger(XmlFactory.class.getName());
    private static final String DISABLE_XML_SECURITY = "com.sun.xml.bind.disableXmlSecurity";
    private static final boolean XML_SECURITY_DISABLED = AccessController.doPrivileged(new PrivilegedAction<Boolean>(){

        @Override
        public Boolean run() {
            return Boolean.getBoolean(XmlFactory.DISABLE_XML_SECURITY);
        }
    });

    private static boolean isXMLSecurityDisabled(boolean runtimeSetting) {
        return XML_SECURITY_DISABLED || runtimeSetting;
    }

    public static SchemaFactory createSchemaFactory(String language, boolean disableSecureProcessing) throws IllegalStateException {
        try {
            SchemaFactory factory2 = SchemaFactory.newInstance(language);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "SchemaFactory instance: {0}", factory2);
            }
            factory2.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", !XmlFactory.isXMLSecurityDisabled(disableSecureProcessing));
            return factory2;
        }
        catch (SAXNotRecognizedException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            throw new IllegalStateException(ex);
        }
        catch (SAXNotSupportedException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            throw new IllegalStateException(ex);
        }
        catch (AbstractMethodError er) {
            LOGGER.log(Level.SEVERE, null, er);
            throw new IllegalStateException(Messages.INVALID_JAXP_IMPLEMENTATION.format(new Object[0]), er);
        }
    }

    public static SAXParserFactory createParserFactory(boolean disableSecureProcessing) throws IllegalStateException {
        try {
            SAXParserFactory factory2 = SAXParserFactory.newInstance();
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "SAXParserFactory instance: {0}", factory2);
            }
            factory2.setNamespaceAware(true);
            factory2.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", !XmlFactory.isXMLSecurityDisabled(disableSecureProcessing));
            return factory2;
        }
        catch (ParserConfigurationException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            throw new IllegalStateException(ex);
        }
        catch (SAXNotRecognizedException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            throw new IllegalStateException(ex);
        }
        catch (SAXNotSupportedException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            throw new IllegalStateException(ex);
        }
        catch (AbstractMethodError er) {
            LOGGER.log(Level.SEVERE, null, er);
            throw new IllegalStateException(Messages.INVALID_JAXP_IMPLEMENTATION.format(new Object[0]), er);
        }
    }

    public static XPathFactory createXPathFactory(boolean disableSecureProcessing) throws IllegalStateException {
        try {
            XPathFactory factory2 = XPathFactory.newInstance();
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "XPathFactory instance: {0}", factory2);
            }
            factory2.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", !XmlFactory.isXMLSecurityDisabled(disableSecureProcessing));
            return factory2;
        }
        catch (XPathFactoryConfigurationException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            throw new IllegalStateException(ex);
        }
        catch (AbstractMethodError er) {
            LOGGER.log(Level.SEVERE, null, er);
            throw new IllegalStateException(Messages.INVALID_JAXP_IMPLEMENTATION.format(new Object[0]), er);
        }
    }

    public static TransformerFactory createTransformerFactory(boolean disableSecureProcessing) throws IllegalStateException {
        try {
            TransformerFactory factory2 = TransformerFactory.newInstance();
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "TransformerFactory instance: {0}", factory2);
            }
            factory2.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", !XmlFactory.isXMLSecurityDisabled(disableSecureProcessing));
            return factory2;
        }
        catch (TransformerConfigurationException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            throw new IllegalStateException(ex);
        }
        catch (AbstractMethodError er) {
            LOGGER.log(Level.SEVERE, null, er);
            throw new IllegalStateException(Messages.INVALID_JAXP_IMPLEMENTATION.format(new Object[0]), er);
        }
    }

    public static DocumentBuilderFactory createDocumentBuilderFactory(boolean disableSecureProcessing) throws IllegalStateException {
        try {
            DocumentBuilderFactory factory2 = DocumentBuilderFactory.newInstance();
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "DocumentBuilderFactory instance: {0}", factory2);
            }
            factory2.setNamespaceAware(true);
            factory2.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", !XmlFactory.isXMLSecurityDisabled(disableSecureProcessing));
            return factory2;
        }
        catch (ParserConfigurationException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            throw new IllegalStateException(ex);
        }
        catch (AbstractMethodError er) {
            LOGGER.log(Level.SEVERE, null, er);
            throw new IllegalStateException(Messages.INVALID_JAXP_IMPLEMENTATION.format(new Object[0]), er);
        }
    }

    public static SchemaFactory allowExternalAccess(SchemaFactory sf, String value, boolean disableSecureProcessing) {
        block7: {
            if (XmlFactory.isXMLSecurityDisabled(disableSecureProcessing)) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, Messages.JAXP_XML_SECURITY_DISABLED.format(new Object[0]));
                }
                return sf;
            }
            if (System.getProperty("javax.xml.accessExternalSchema") != null) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, Messages.JAXP_EXTERNAL_ACCESS_CONFIGURED.format(new Object[0]));
                }
                return sf;
            }
            try {
                sf.setProperty(ACCESS_EXTERNAL_SCHEMA, value);
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, Messages.JAXP_SUPPORTED_PROPERTY.format(ACCESS_EXTERNAL_SCHEMA));
                }
            }
            catch (SAXException ignored) {
                if (!LOGGER.isLoggable(Level.CONFIG)) break block7;
                LOGGER.log(Level.CONFIG, Messages.JAXP_UNSUPPORTED_PROPERTY.format(ACCESS_EXTERNAL_SCHEMA), ignored);
            }
        }
        return sf;
    }

    public static SchemaFactory allowExternalDTDAccess(SchemaFactory sf, String value, boolean disableSecureProcessing) {
        block7: {
            if (XmlFactory.isXMLSecurityDisabled(disableSecureProcessing)) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, Messages.JAXP_XML_SECURITY_DISABLED.format(new Object[0]));
                }
                return sf;
            }
            if (System.getProperty("javax.xml.accessExternalDTD") != null) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, Messages.JAXP_EXTERNAL_ACCESS_CONFIGURED.format(new Object[0]));
                }
                return sf;
            }
            try {
                sf.setProperty(ACCESS_EXTERNAL_DTD, value);
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, Messages.JAXP_SUPPORTED_PROPERTY.format(ACCESS_EXTERNAL_DTD));
                }
            }
            catch (SAXException ignored) {
                if (!LOGGER.isLoggable(Level.CONFIG)) break block7;
                LOGGER.log(Level.CONFIG, Messages.JAXP_UNSUPPORTED_PROPERTY.format(ACCESS_EXTERNAL_DTD), ignored);
            }
        }
        return sf;
    }
}

