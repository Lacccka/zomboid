/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.integration.doc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;
import se.krka.kahlua.integration.doc.ApiInformation;
import se.krka.kahlua.integration.expose.MethodDebugInformation;
import se.krka.kahlua.integration.expose.MethodParameter;

public class DokuWikiPrinter {
    private final ApiInformation information;
    private final PrintWriter writer;

    public DokuWikiPrinter(File output, ApiInformation information) throws IOException {
        this(new FileWriter(output), information);
    }

    public DokuWikiPrinter(Writer writer, ApiInformation information) {
        this.information = information;
        this.writer = new PrintWriter(writer);
    }

    public void process() {
        this.printClassHierarchy();
        this.printFunctions();
        this.writer.close();
    }

    private void printFunctions() {
        this.writer.println("====== Global functions ======");
        List<Class<?>> classes = this.information.getAllClasses();
        for (Class<?> clazz : classes) {
            this.printClassFunctions(clazz);
        }
    }

    private void printClassFunctions(Class<?> clazz) {
        List<MethodDebugInformation> functionsForClass = this.information.getFunctionsForClass(clazz);
        if (!functionsForClass.isEmpty()) {
            this.writer.printf("===== %s ====\n", clazz.getSimpleName());
            this.writer.printf("In package: %s\n", clazz.getPackage().getName());
            for (MethodDebugInformation methodInfo : functionsForClass) {
                this.printFunction(methodInfo, "====");
            }
            this.writer.printf("\n----\n\n", new Object[0]);
        }
    }

    private void printFunction(MethodDebugInformation methodInfo, String heading) {
        this.writer.printf("%s %s %s\n", heading, methodInfo.getLuaName(), heading);
        this.writer.printf("<code lua>%s</code>\n", methodInfo.getLuaDescription());
        for (MethodParameter parameter : methodInfo.getParameters()) {
            String name = parameter.getName();
            String type = parameter.getType();
            String description = parameter.getDescription();
            if (description == null) {
                this.writer.printf("  - **''%s''** ''%s''\n", type, name);
                continue;
            }
            this.writer.printf("  - **''%s''** ''%s'': %s\n", type, name, description);
        }
        String returnDescription = methodInfo.getReturnDescription();
        if (returnDescription == null) {
            this.writer.printf("  * returns ''%s''\n", methodInfo.getReturnType());
        } else {
            this.writer.printf("  * returns ''%s'': %s\n", methodInfo.getReturnType(), returnDescription);
        }
    }

    private void printClassHierarchy() {
        this.writer.println("====== Class hierarchy ======");
        List<Class<?>> roots = this.information.getRootClasses();
        for (Class<?> root : roots) {
            this.printClassHierarchy(root, null);
        }
    }

    private void printClassHierarchy(Class<?> clazz, Class<?> parent) {
        List<Class<?>> children = this.information.getChildrenForClass(clazz);
        List<MethodDebugInformation> methodsForClass = this.information.getMethodsForClass(clazz);
        if (!children.isEmpty() || !methodsForClass.isEmpty() || parent != null) {
            this.writer.printf("===== %s =====\n", clazz.getSimpleName());
            this.writer.printf("In package: ''%s''\n", clazz.getPackage().getName());
            if (parent != null) {
                this.writer.printf("\nSubclass of [[#%s|%s]]\n", parent.getSimpleName(), parent.getSimpleName());
            }
            if (!children.isEmpty()) {
                this.writer.printf("\nChildren: ", new Object[0]);
                boolean needsComma = false;
                for (Class<?> child : children) {
                    if (needsComma) {
                        this.writer.print(", ");
                    } else {
                        needsComma = true;
                    }
                    this.writer.printf("[[#%s|%s]]", child.getSimpleName(), child.getSimpleName());
                }
            }
            this.printMethods(clazz);
            this.writer.printf("\n----\n\n", new Object[0]);
            for (Class<?> child : children) {
                this.printClassHierarchy(child, clazz);
            }
        }
    }

    private void printMethods(Class<?> clazz) {
        List<MethodDebugInformation> methodsForClass = this.information.getMethodsForClass(clazz);
        if (!methodsForClass.isEmpty()) {
            for (MethodDebugInformation methodInfo : methodsForClass) {
                this.printFunction(methodInfo, "====");
            }
        }
    }
}

