/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.impl;

import java.security.AccessController;
import java.security.PrivilegedAction;

class SecureLoader {
    SecureLoader() {
    }

    static ClassLoader getContextClassLoader() {
        if (System.getSecurityManager() == null) {
            return Thread.currentThread().getContextClassLoader();
        }
        return (ClassLoader)AccessController.doPrivileged(new PrivilegedAction(){

            public Object run() {
                return Thread.currentThread().getContextClassLoader();
            }
        });
    }

    static ClassLoader getClassClassLoader(final Class c) {
        if (System.getSecurityManager() == null) {
            return c.getClassLoader();
        }
        return (ClassLoader)AccessController.doPrivileged(new PrivilegedAction(){

            public Object run() {
                return c.getClassLoader();
            }
        });
    }

    static ClassLoader getSystemClassLoader() {
        if (System.getSecurityManager() == null) {
            return ClassLoader.getSystemClassLoader();
        }
        return (ClassLoader)AccessController.doPrivileged(new PrivilegedAction(){

            public Object run() {
                return ClassLoader.getSystemClassLoader();
            }
        });
    }
}

