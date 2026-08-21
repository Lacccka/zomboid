/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.profiler;

import se.krka.kahlua.profiler.StacktraceElement;
import se.krka.kahlua.vm.Prototype;

public class LuaStacktraceElement
implements StacktraceElement {
    private final int pc;
    private final Prototype prototype;

    public LuaStacktraceElement(int pc, Prototype prototype) {
        this.pc = pc;
        this.prototype = prototype;
    }

    public int getLine() {
        if (this.pc >= 0 && this.pc < this.prototype.lines.length) {
            return this.prototype.lines[this.pc];
        }
        return 0;
    }

    public String getSource() {
        return this.prototype.name;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LuaStacktraceElement)) {
            return false;
        }
        LuaStacktraceElement that = (LuaStacktraceElement)o;
        if (this.getLine() != that.getLine()) {
            return false;
        }
        return this.prototype.equals(that.prototype);
    }

    public int hashCode() {
        int result = this.getLine();
        result = 31 * result + this.prototype.hashCode();
        return result;
    }

    public String toString() {
        return this.name();
    }

    @Override
    public String name() {
        return this.getSource() + ":" + this.getLine();
    }

    @Override
    public String type() {
        return "lua";
    }
}

