/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.output;

import org.apache.commons.io.output.UncheckedAppendableImpl;

public interface UncheckedAppendable
extends Appendable {
    public static UncheckedAppendable on(Appendable appendable) {
        return new UncheckedAppendableImpl(appendable);
    }

    @Override
    public UncheckedAppendable append(char var1);

    @Override
    public UncheckedAppendable append(CharSequence var1);

    @Override
    public UncheckedAppendable append(CharSequence var1, int var2, int var3);
}

