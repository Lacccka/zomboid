/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.spi;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.spi.MutableThreadContextStack;
import org.apache.logging.log4j.spi.ThreadContextStack;
import org.apache.logging.log4j.util.StringBuilderFormattable;
import org.apache.logging.log4j.util.StringBuilders;
import org.apache.logging.log4j.util.Strings;

public class DefaultThreadContextStack
implements ThreadContextStack,
StringBuilderFormattable {
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    private static final long serialVersionUID = 5050501L;
    private static final ThreadLocal<MutableThreadContextStack> STACK = new ThreadLocal();
    private final boolean useStack;

    public DefaultThreadContextStack(boolean useStack) {
        this.useStack = useStack;
    }

    private MutableThreadContextStack getNonNullStackCopy() {
        MutableThreadContextStack values2 = STACK.get();
        return values2 == null ? new MutableThreadContextStack() : values2.copy();
    }

    @Override
    public boolean add(String s) {
        if (!this.useStack) {
            return false;
        }
        MutableThreadContextStack copy = this.getNonNullStackCopy();
        copy.add(s);
        copy.freeze();
        STACK.set(copy);
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends String> strings) {
        if (!this.useStack || strings.isEmpty()) {
            return false;
        }
        MutableThreadContextStack copy = this.getNonNullStackCopy();
        copy.addAll(strings);
        copy.freeze();
        STACK.set(copy);
        return true;
    }

    @Override
    public List<String> asList() {
        MutableThreadContextStack values2 = STACK.get();
        if (values2 == null) {
            return Collections.emptyList();
        }
        return values2.asList();
    }

    @Override
    public void clear() {
        STACK.remove();
    }

    @Override
    public boolean contains(Object o) {
        MutableThreadContextStack values2 = STACK.get();
        return values2 != null && values2.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> objects) {
        if (objects.isEmpty()) {
            return true;
        }
        MutableThreadContextStack values2 = STACK.get();
        return values2 != null && values2.containsAll(objects);
    }

    @Override
    public ThreadContextStack copy() {
        MutableThreadContextStack values2 = null;
        if (!this.useStack || (values2 = STACK.get()) == null) {
            return new MutableThreadContextStack();
        }
        return values2.copy();
    }

    @Override
    public boolean equals(Object obj) {
        ThreadContextStack other;
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof DefaultThreadContextStack) {
            other = (DefaultThreadContextStack)obj;
            if (this.useStack != other.useStack) {
                return false;
            }
        }
        if (!(obj instanceof ThreadContextStack)) {
            return false;
        }
        other = (ThreadContextStack)obj;
        MutableThreadContextStack values2 = STACK.get();
        if (values2 == null) {
            return false;
        }
        return values2.equals(other);
    }

    @Override
    public int getDepth() {
        MutableThreadContextStack values2 = STACK.get();
        return values2 == null ? 0 : values2.getDepth();
    }

    @Override
    public int hashCode() {
        MutableThreadContextStack values2 = STACK.get();
        int prime = 31;
        int result = 1;
        result = 31 * result + (values2 == null ? 0 : values2.hashCode());
        return result;
    }

    @Override
    public boolean isEmpty() {
        MutableThreadContextStack values2 = STACK.get();
        return values2 == null || values2.isEmpty();
    }

    @Override
    public Iterator<String> iterator() {
        MutableThreadContextStack values2 = STACK.get();
        if (values2 == null) {
            List empty = Collections.emptyList();
            return empty.iterator();
        }
        return values2.iterator();
    }

    @Override
    public String peek() {
        MutableThreadContextStack values2 = STACK.get();
        if (values2 == null || values2.isEmpty()) {
            return "";
        }
        return values2.peek();
    }

    @Override
    public String pop() {
        if (!this.useStack) {
            return "";
        }
        MutableThreadContextStack values2 = STACK.get();
        if (values2 == null || values2.isEmpty()) {
            return "";
        }
        MutableThreadContextStack copy = (MutableThreadContextStack)values2.copy();
        String result = copy.pop();
        copy.freeze();
        STACK.set(copy);
        return result;
    }

    @Override
    public void push(String message) {
        if (!this.useStack) {
            return;
        }
        this.add(message);
    }

    @Override
    public boolean remove(Object o) {
        if (!this.useStack) {
            return false;
        }
        MutableThreadContextStack values2 = STACK.get();
        if (values2 == null || values2.isEmpty()) {
            return false;
        }
        MutableThreadContextStack copy = (MutableThreadContextStack)values2.copy();
        boolean result = copy.remove(o);
        copy.freeze();
        STACK.set(copy);
        return result;
    }

    @Override
    public boolean removeAll(Collection<?> objects) {
        if (!this.useStack || objects.isEmpty()) {
            return false;
        }
        MutableThreadContextStack values2 = STACK.get();
        if (values2 == null || values2.isEmpty()) {
            return false;
        }
        MutableThreadContextStack copy = (MutableThreadContextStack)values2.copy();
        boolean result = copy.removeAll(objects);
        copy.freeze();
        STACK.set(copy);
        return result;
    }

    @Override
    public boolean retainAll(Collection<?> objects) {
        if (!this.useStack || objects.isEmpty()) {
            return false;
        }
        MutableThreadContextStack values2 = STACK.get();
        if (values2 == null || values2.isEmpty()) {
            return false;
        }
        MutableThreadContextStack copy = (MutableThreadContextStack)values2.copy();
        boolean result = copy.retainAll(objects);
        copy.freeze();
        STACK.set(copy);
        return result;
    }

    @Override
    public int size() {
        MutableThreadContextStack values2 = STACK.get();
        return values2 == null ? 0 : values2.size();
    }

    @Override
    public Object[] toArray() {
        MutableThreadContextStack result = STACK.get();
        if (result == null) {
            return Strings.EMPTY_ARRAY;
        }
        return result.toArray(EMPTY_OBJECT_ARRAY);
    }

    @Override
    public <T> T[] toArray(T[] ts) {
        MutableThreadContextStack result = STACK.get();
        if (result == null) {
            if (ts.length > 0) {
                ts[0] = null;
            }
            return ts;
        }
        return result.toArray(ts);
    }

    public String toString() {
        MutableThreadContextStack values2 = STACK.get();
        return values2 == null ? "[]" : values2.toString();
    }

    @Override
    public void formatTo(StringBuilder buffer) {
        MutableThreadContextStack values2 = STACK.get();
        if (values2 == null) {
            buffer.append("[]");
        } else {
            StringBuilders.appendValue(buffer, values2);
        }
    }

    @Override
    public void trim(int depth) {
        if (depth < 0) {
            throw new IllegalArgumentException("Maximum stack depth cannot be negative");
        }
        MutableThreadContextStack values2 = STACK.get();
        if (values2 == null) {
            return;
        }
        MutableThreadContextStack copy = (MutableThreadContextStack)values2.copy();
        copy.trim(depth);
        copy.freeze();
        STACK.set(copy);
    }

    @Override
    public ThreadContext.ContextStack getImmutableStackOrNull() {
        return STACK.get();
    }
}

