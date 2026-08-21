/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.filefilter;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Objects;
import org.apache.commons.io.filefilter.AbstractFileFilter;

public class PathMatcherFileFilter
extends AbstractFileFilter {
    private final PathMatcher pathMatcher;

    public PathMatcherFileFilter(PathMatcher pathMatcher) {
        this.pathMatcher = Objects.requireNonNull(pathMatcher, "pathMatcher");
    }

    @Override
    public boolean accept(File file) {
        return file != null && this.matches(file.toPath());
    }

    @Override
    public boolean matches(Path path) {
        return this.pathMatcher.matches(path);
    }
}

