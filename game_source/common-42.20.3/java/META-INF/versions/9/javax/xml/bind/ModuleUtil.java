/*
 * Decompiled with CFR 0.152.
 */
package javax.xml.bind;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Messages;

/*
 * Multiple versions of this class in jar - see https://www.benf.org/other/cfr/multi-version-jar.html
 */
class ModuleUtil {
    private static Logger logger = Logger.getLogger("javax.xml.bind");

    ModuleUtil() {
    }

    static Class[] getClassesFromContextPath(String string, ClassLoader classLoader) throws JAXBException {
        String[] stringArray;
        ArrayList<Class> arrayList = new ArrayList<Class>();
        if (string == null || string.isEmpty()) {
            return arrayList.toArray(new Class[0]);
        }
        for (String string2 : stringArray = string.split(":")) {
            try {
                Class<?> clazz = classLoader.loadClass(string2 + ".ObjectFactory");
                arrayList.add(clazz);
            }
            catch (ClassNotFoundException classNotFoundException) {
                try {
                    Class clazz = ModuleUtil.findFirstByJaxbIndex(string2, classLoader);
                    if (clazz == null) continue;
                    arrayList.add(clazz);
                }
                catch (IOException iOException) {
                    throw new JAXBException(iOException);
                }
            }
        }
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Resolved classes from context path: {0}", arrayList);
        }
        return arrayList.toArray(new Class[0]);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    static Class findFirstByJaxbIndex(String string, ClassLoader classLoader) throws IOException, JAXBException {
        String string2 = string.replace('.', '/') + "/jaxb.index";
        InputStream inputStream = classLoader.getResourceAsStream(string2);
        if (inputStream == null) {
            return null;
        }
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));){
            String string3 = bufferedReader.readLine();
            while (string3 != null) {
                if ((string3 = string3.trim()).startsWith("#") || string3.length() == 0) {
                    string3 = bufferedReader.readLine();
                    continue;
                }
                try {
                    Class<?> clazz = classLoader.loadClass(string + "." + string3);
                    return clazz;
                }
                catch (ClassNotFoundException classNotFoundException) {
                    try {
                        throw new JAXBException(Messages.format("ContextFinder.ErrorLoadClass", string3, string), classNotFoundException);
                    }
                    catch (Throwable throwable) {
                        throw throwable;
                        return null;
                    }
                }
            }
        }
    }

    public static void delegateAddOpensToImplModule(Class[] classArray, Class<?> clazz) throws JAXBException {
        Module module = clazz.getModule();
        Module module2 = JAXBContext.class.getModule();
        for (Class<?> clazz2 : classArray) {
            Class<?> clazz3 = clazz2.isArray() ? clazz2.getComponentType() : clazz2;
            Module module3 = clazz3.getModule();
            String string = clazz3.getPackageName();
            if (!module3.isNamed() || module3.getName().equals("java.base")) continue;
            if (!module3.isOpen(string, module2)) {
                throw new JAXBException(Messages.format("JAXBClasses.notOpen", string, clazz3.getName(), module3.getName()));
            }
            module3.addOpens(string, module);
            if (!logger.isLoggable(Level.FINE)) continue;
            logger.log(Level.FINE, "Propagating openness of package {0} in {1} to {2}.", new String[]{string, module3.getName(), module.getName()});
        }
    }
}

