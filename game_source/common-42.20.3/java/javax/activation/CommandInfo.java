/*
 * Decompiled with CFR 0.152.
 */
package javax.activation;

import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.activation.CommandObject;
import javax.activation.DataHandler;

public class CommandInfo {
    private String verb;
    private String className;

    public CommandInfo(String verb, String className) {
        this.verb = verb;
        this.className = className;
    }

    public String getCommandName() {
        return this.verb;
    }

    public String getCommandClass() {
        return this.className;
    }

    public Object getCommandObject(DataHandler dh, ClassLoader loader) throws IOException, ClassNotFoundException {
        Object new_bean = null;
        new_bean = Beans.instantiate(loader, this.className);
        if (new_bean != null) {
            InputStream is;
            if (new_bean instanceof CommandObject) {
                ((CommandObject)new_bean).setCommandContext(this.verb, dh);
            } else if (new_bean instanceof Externalizable && dh != null && (is = dh.getInputStream()) != null) {
                ((Externalizable)new_bean).readExternal(new ObjectInputStream(is));
            }
        }
        return new_bean;
    }

    private static final class Beans {
        static final Method instantiateMethod;

        private Beans() {
        }

        static Object instantiate(ClassLoader loader, String cn) throws IOException, ClassNotFoundException {
            block10: {
                if (instantiateMethod != null) {
                    try {
                        return instantiateMethod.invoke(null, loader, cn);
                    }
                    catch (InvocationTargetException e) {
                        InvocationTargetException exception = e;
                        break block10;
                    }
                    catch (IllegalAccessException e) {
                        IllegalAccessException exception = e;
                        break block10;
                    }
                }
                SecurityManager security = System.getSecurityManager();
                if (security != null) {
                    int i;
                    int b;
                    String cname = cn.replace('/', '.');
                    if (cname.startsWith("[") && (b = cname.lastIndexOf(91) + 2) > 1 && b < cname.length()) {
                        cname = cname.substring(b);
                    }
                    if ((i = cname.lastIndexOf(46)) != -1) {
                        security.checkPackageAccess(cname.substring(0, i));
                    }
                }
                if (loader == null) {
                    loader = (ClassLoader)AccessController.doPrivileged(new PrivilegedAction(){

                        public Object run() {
                            ClassLoader cl = null;
                            try {
                                cl = ClassLoader.getSystemClassLoader();
                            }
                            catch (SecurityException securityException) {
                                // empty catch block
                            }
                            return cl;
                        }
                    });
                }
                Class<?> beanClass = Class.forName(cn, true, loader);
                try {
                    return beanClass.newInstance();
                }
                catch (Exception ex) {
                    throw new ClassNotFoundException(beanClass + ": " + ex, ex);
                }
            }
            return null;
        }

        static {
            Method m;
            try {
                Class<?> c = Class.forName("java.beans.Beans");
                m = c.getDeclaredMethod("instantiate", ClassLoader.class, String.class);
            }
            catch (ClassNotFoundException e) {
                m = null;
            }
            catch (NoSuchMethodException e) {
                m = null;
            }
            instantiateMethod = m;
        }
    }
}

