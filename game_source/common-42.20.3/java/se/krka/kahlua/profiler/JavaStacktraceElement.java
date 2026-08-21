/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.profiler;

import se.krka.kahlua.profiler.StacktraceElement;
import se.krka.kahlua.vm.JavaFunction;

public class JavaStacktraceElement
implements StacktraceElement {
    private final JavaFunction javaFunction;

    public JavaStacktraceElement(JavaFunction javaFunction) {
        this.javaFunction = javaFunction;
    }

    @Override
    public String name() {
        return this.javaFunction.toString();
    }

    @Override
    public String type() {
        return "java";
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof JavaStacktraceElement)) {
            return false;
        }
        JavaStacktraceElement that = (JavaStacktraceElement)o;
        return this.javaFunction == that.javaFunction;
    }

    public int hashCode() {
        return this.javaFunction.hashCode();
    }
}

