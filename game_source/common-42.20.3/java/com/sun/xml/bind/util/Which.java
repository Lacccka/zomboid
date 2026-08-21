/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.util;

import com.sun.xml.bind.util.SecureLoader;
import java.net.URL;

public class Which {
    public static String which(Class clazz) {
        return Which.which(clazz.getName(), SecureLoader.getClassClassLoader(clazz));
    }

    public static String which(String classname, ClassLoader loader) {
        URL it;
        String classnameAsResource = classname.replace('.', '/') + ".class";
        if (loader == null) {
            loader = SecureLoader.getSystemClassLoader();
        }
        if ((it = loader.getResource(classnameAsResource)) != null) {
            return it.toString();
        }
        return null;
    }
}

