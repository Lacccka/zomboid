/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.compressors.pack200;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import org.apache.commons.compress.java.util.jar.Pack200;

public class Pack200Utils {
    public static void normalize(File jar) throws IOException {
        Pack200Utils.normalize(jar, jar, null);
    }

    public static void normalize(File from, File to) throws IOException {
        Pack200Utils.normalize(from, to, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void normalize(File from, File to, Map<String, String> props) throws IOException {
        if (props == null) {
            props = new HashMap<String, String>();
        }
        props.put("pack.segment.limit", "-1");
        Path tempFile = Files.createTempFile("commons-compress", "pack200normalize", new FileAttribute[0]);
        try {
            try (OutputStream fos = Files.newOutputStream(tempFile, new OpenOption[0]);
                 JarFile jarFile = new JarFile(from);){
                Pack200.Packer packer = Pack200.newPacker();
                packer.properties().putAll(props);
                packer.pack(jarFile, fos);
            }
            Pack200.Unpacker unpacker = Pack200.newUnpacker();
            try (JarOutputStream jos = new JarOutputStream(Files.newOutputStream(to.toPath(), new OpenOption[0]));){
                unpacker.unpack(tempFile.toFile(), jos);
            }
        }
        finally {
            Files.delete(tempFile);
        }
    }

    public static void normalize(File jar, Map<String, String> props) throws IOException {
        Pack200Utils.normalize(jar, jar, props);
    }

    private Pack200Utils() {
    }
}

