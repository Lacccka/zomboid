/*
 * Decompiled with CFR 0.152.
 */
package javax.activation;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Enumeration;

class SecuritySupport {
    private SecuritySupport() {
    }

    public static ClassLoader getContextClassLoader() {
        return (ClassLoader)AccessController.doPrivileged(new PrivilegedAction(){

            public Object run() {
                ClassLoader cl = null;
                try {
                    cl = Thread.currentThread().getContextClassLoader();
                }
                catch (SecurityException securityException) {
                    // empty catch block
                }
                return cl;
            }
        });
    }

    public static InputStream getResourceAsStream(final Class c, final String name) throws IOException {
        try {
            return (InputStream)AccessController.doPrivileged(new PrivilegedExceptionAction(){

                public Object run() throws IOException {
                    return c.getResourceAsStream(name);
                }
            });
        }
        catch (PrivilegedActionException e) {
            throw (IOException)e.getException();
        }
    }

    public static URL[] getResources(final ClassLoader cl, final String name) {
        return (URL[])AccessController.doPrivileged(new PrivilegedAction(){

            public Object run() {
                URL[] ret = null;
                try {
                    ArrayList<URL> v = new ArrayList<URL>();
                    Enumeration<URL> e = cl.getResources(name);
                    while (e != null && e.hasMoreElements()) {
                        URL url = e.nextElement();
                        if (url == null) continue;
                        v.add(url);
                    }
                    if (v.size() > 0) {
                        ret = new URL[v.size()];
                        ret = v.toArray(ret);
                    }
                }
                catch (IOException iOException) {
                }
                catch (SecurityException securityException) {
                    // empty catch block
                }
                return ret;
            }
        });
    }

    public static URL[] getSystemResources(final String name) {
        return (URL[])AccessController.doPrivileged(new PrivilegedAction(){

            public Object run() {
                URL[] ret = null;
                try {
                    ArrayList<URL> v = new ArrayList<URL>();
                    Enumeration<URL> e = ClassLoader.getSystemResources(name);
                    while (e != null && e.hasMoreElements()) {
                        URL url = e.nextElement();
                        if (url == null) continue;
                        v.add(url);
                    }
                    if (v.size() > 0) {
                        ret = new URL[v.size()];
                        ret = v.toArray(ret);
                    }
                }
                catch (IOException iOException) {
                }
                catch (SecurityException securityException) {
                    // empty catch block
                }
                return ret;
            }
        });
    }

    public static InputStream openStream(final URL url) throws IOException {
        try {
            return (InputStream)AccessController.doPrivileged(new PrivilegedExceptionAction(){

                public Object run() throws IOException {
                    return url.openStream();
                }
            });
        }
        catch (PrivilegedActionException e) {
            throw (IOException)e.getException();
        }
    }
}

