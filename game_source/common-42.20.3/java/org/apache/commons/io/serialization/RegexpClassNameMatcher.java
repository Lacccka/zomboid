/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.serialization;

import java.util.Objects;
import java.util.regex.Pattern;
import org.apache.commons.io.serialization.ClassNameMatcher;

final class RegexpClassNameMatcher
implements ClassNameMatcher {
    private final Pattern pattern;

    public RegexpClassNameMatcher(Pattern pattern) {
        this.pattern = Objects.requireNonNull(pattern, "pattern");
    }

    public RegexpClassNameMatcher(String regex) {
        this(Pattern.compile(regex));
    }

    @Override
    public boolean matches(String className) {
        return this.pattern.matcher(className).matches();
    }
}

