/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system.macosx;

import org.jspecify.annotations.Nullable;
import org.lwjgl.system.Callback;
import org.lwjgl.system.macosx.EnumerationMutationHandlerI;

public abstract class EnumerationMutationHandler
extends Callback
implements EnumerationMutationHandlerI {
    public static EnumerationMutationHandler create(long functionPointer) {
        EnumerationMutationHandlerI instance = (EnumerationMutationHandlerI)Callback.get(functionPointer);
        return instance instanceof EnumerationMutationHandler ? (EnumerationMutationHandler)instance : new Container(functionPointer, instance);
    }

    public static @Nullable EnumerationMutationHandler createSafe(long functionPointer) {
        return functionPointer == 0L ? null : EnumerationMutationHandler.create(functionPointer);
    }

    public static EnumerationMutationHandler create(EnumerationMutationHandlerI instance) {
        return instance instanceof EnumerationMutationHandler ? (EnumerationMutationHandler)instance : new Container(instance.address(), instance);
    }

    protected EnumerationMutationHandler() {
        super(DESCRIPTOR);
    }

    EnumerationMutationHandler(long functionPointer) {
        super(functionPointer);
    }

    private static final class Container
    extends EnumerationMutationHandler {
        private final EnumerationMutationHandlerI delegate;

        Container(long functionPointer, EnumerationMutationHandlerI delegate) {
            super(functionPointer);
            this.delegate = delegate;
        }

        @Override
        public void invoke(long id) {
            this.delegate.invoke(id);
        }
    }
}

