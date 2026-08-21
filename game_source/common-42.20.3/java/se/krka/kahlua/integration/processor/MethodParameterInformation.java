/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.integration.processor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class MethodParameterInformation
implements Serializable {
    public static final MethodParameterInformation EMPTY = new MethodParameterInformation(Collections.EMPTY_LIST);
    private static final long serialVersionUID = -3059552311721486815L;
    private final List<String> parameterNames;

    public MethodParameterInformation(List<String> parameterNames) {
        this.parameterNames = parameterNames;
    }

    public String getName(int index) {
        if (index >= this.parameterNames.size()) {
            return "arg" + (index + 1);
        }
        return this.parameterNames.get(index);
    }
}

