/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.filefilter;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.Serializable;
import java.util.Objects;
import org.apache.commons.io.filefilter.AbstractFileFilter;

public class DelegateFileFilter
extends AbstractFileFilter
implements Serializable {
    private static final long serialVersionUID = -8723373124984771318L;
    private final transient FileFilter fileFilter;
    private final transient FilenameFilter fileNameFilter;

    public DelegateFileFilter(FileFilter fileFilter) {
        Objects.requireNonNull(fileFilter, "filter");
        this.fileFilter = fileFilter;
        this.fileNameFilter = null;
    }

    public DelegateFileFilter(FilenameFilter fileNameFilter) {
        Objects.requireNonNull(fileNameFilter, "filter");
        this.fileNameFilter = fileNameFilter;
        this.fileFilter = null;
    }

    @Override
    public boolean accept(File file) {
        if (this.fileFilter != null) {
            return this.fileFilter.accept(file);
        }
        return super.accept(file);
    }

    @Override
    public boolean accept(File dir, String name) {
        if (this.fileNameFilter != null) {
            return this.fileNameFilter.accept(dir, name);
        }
        return super.accept(dir, name);
    }

    @Override
    public String toString() {
        String delegate = this.fileFilter != null ? this.fileFilter.toString() : this.fileNameFilter.toString();
        return super.toString() + "(" + delegate + ")";
    }
}

