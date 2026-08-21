/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.reflect.opt;

import com.sun.xml.bind.Util;
import com.sun.xml.bind.v2.bytecode.ClassTailor;
import com.sun.xml.bind.v2.runtime.reflect.opt.SecureLoader;
import java.io.InputStream;
import java.util.logging.Logger;

class AccessorInjector {
    private static final Logger logger = Util.getClassLogger();
    protected static final boolean noOptimize;
    private static final ClassLoader CLASS_LOADER;

    AccessorInjector() {
    }

    private static byte[] tailor(String templateClassName, String newClassName, String ... replacements) {
        InputStream resource = CLASS_LOADER != null ? CLASS_LOADER.getResourceAsStream(templateClassName + ".class") : ClassLoader.getSystemResourceAsStream(templateClassName + ".class");
        if (resource == null) {
            return null;
        }
        return ClassTailor.tailor(resource, templateClassName, newClassName, replacements);
    }

    static {
        boolean bl = noOptimize = Util.getSystemProperty(ClassTailor.class.getName() + ".noOptimize") != null;
        if (noOptimize) {
            logger.info("The optimized code generation is disabled");
        }
        CLASS_LOADER = SecureLoader.getClassClassLoader(AccessorInjector.class);
    }
}

