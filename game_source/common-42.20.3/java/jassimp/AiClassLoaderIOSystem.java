/*
 * Decompiled with CFR 0.152.
 */
package jassimp;

import jassimp.AiIOSystem;
import jassimp.AiInputStreamIOStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class AiClassLoaderIOSystem
implements AiIOSystem<AiInputStreamIOStream> {
    private final Class<?> clazz;
    private final ClassLoader classLoader;

    public AiClassLoaderIOSystem(ClassLoader classLoader) {
        this.clazz = null;
        this.classLoader = classLoader;
    }

    public AiClassLoaderIOSystem(Class<?> clazz) {
        this.clazz = clazz;
        this.classLoader = null;
    }

    @Override
    public AiInputStreamIOStream open(String string, String string2) {
        try {
            InputStream inputStream2;
            if (this.clazz != null) {
                inputStream2 = this.clazz.getResourceAsStream(string);
            } else if (this.classLoader != null) {
                inputStream2 = this.classLoader.getResourceAsStream(string);
            } else {
                System.err.println("[" + this.getClass().getSimpleName() + "] No class or classLoader provided to resolve " + string);
                return null;
            }
            if (inputStream2 != null) {
                return new AiInputStreamIOStream(inputStream2);
            }
            System.err.println("[" + this.getClass().getSimpleName() + "] Cannot find " + string);
            return null;
        }
        catch (IOException iOException) {
            iOException.printStackTrace();
            return null;
        }
    }

    @Override
    public void close(AiInputStreamIOStream aiInputStreamIOStream) {
    }

    @Override
    public boolean exists(String string) {
        URL uRL = null;
        if (this.clazz != null) {
            uRL = this.clazz.getResource(string);
        } else if (this.classLoader != null) {
            uRL = this.classLoader.getResource(string);
        }
        return uRL != null;
    }

    @Override
    public char getOsSeparator() {
        return '/';
    }
}

