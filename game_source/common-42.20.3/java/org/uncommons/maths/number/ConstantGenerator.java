/*
 * Decompiled with CFR 0.152.
 */
package org.uncommons.maths.number;

import org.uncommons.maths.number.NumberGenerator;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class ConstantGenerator<T extends Number>
implements NumberGenerator<T> {
    private final T constant;

    public ConstantGenerator(T constant) {
        this.constant = constant;
    }

    @Override
    public T nextValue() {
        return this.constant;
    }
}

