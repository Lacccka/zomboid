/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.harmony.unpack200.bytecode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import org.apache.commons.compress.harmony.unpack200.Segment;
import org.apache.commons.compress.harmony.unpack200.bytecode.ByteCode;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPClass;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPDouble;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPLong;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPUTF8;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassFileEntry;
import org.apache.commons.compress.harmony.unpack200.bytecode.ConstantPoolEntry;

public class ClassConstantPool {
    protected HashSet<ClassFileEntry> entriesContainsSet = new HashSet();
    protected HashSet<ClassFileEntry> othersContainsSet = new HashSet();
    private final HashSet<ClassFileEntry> mustStartClassPool = new HashSet();
    protected Map<ClassFileEntry, Integer> indexCache;
    private final List<ClassFileEntry> others = new ArrayList<ClassFileEntry>(500);
    private final List<ClassFileEntry> entries = new ArrayList<ClassFileEntry>(500);
    private boolean resolved;

    public ClassFileEntry add(ClassFileEntry entry) {
        if (entry instanceof ByteCode) {
            return null;
        }
        if (entry instanceof ConstantPoolEntry) {
            if (this.entriesContainsSet.add(entry)) {
                this.entries.add(entry);
            }
        } else if (this.othersContainsSet.add(entry)) {
            this.others.add(entry);
        }
        return entry;
    }

    public void addNestedEntries() {
        boolean added = true;
        ArrayList<ClassFileEntry> parents = new ArrayList<ClassFileEntry>(512);
        ArrayList<ClassFileEntry> children = new ArrayList<ClassFileEntry>(512);
        parents.addAll(this.entries);
        parents.addAll(this.others);
        while (added || parents.size() > 0) {
            children.clear();
            int entriesOriginalSize = this.entries.size();
            int othersOriginalSize = this.others.size();
            for (int indexParents = 0; indexParents < parents.size(); ++indexParents) {
                boolean isAtStart;
                ClassFileEntry entry = (ClassFileEntry)parents.get(indexParents);
                ClassFileEntry[] entryChildren = entry.getNestedClassFileEntries();
                children.addAll(Arrays.asList(entryChildren));
                boolean bl = isAtStart = entry instanceof ByteCode && ((ByteCode)entry).nestedMustStartClassPool();
                if (isAtStart) {
                    this.mustStartClassPool.addAll(Arrays.asList(entryChildren));
                }
                this.add(entry);
            }
            added = this.entries.size() != entriesOriginalSize || this.others.size() != othersOriginalSize;
            parents.clear();
            parents.addAll(children);
        }
    }

    public ClassFileEntry addWithNestedEntries(ClassFileEntry entry) {
        this.add(entry);
        for (ClassFileEntry nestedEntry : entry.getNestedClassFileEntries()) {
            this.addWithNestedEntries(nestedEntry);
        }
        return entry;
    }

    public List<ClassFileEntry> entries() {
        return Collections.unmodifiableList(this.entries);
    }

    public ClassFileEntry get(int i) {
        if (!this.resolved) {
            throw new IllegalStateException("Constant pool is not yet resolved; this does not make any sense");
        }
        return this.entries.get(--i);
    }

    public int indexOf(ClassFileEntry entry) {
        if (!this.resolved) {
            throw new IllegalStateException("Constant pool is not yet resolved; this does not make any sense");
        }
        if (null == this.indexCache) {
            throw new IllegalStateException("Index cache is not initialized!");
        }
        Integer entryIndex = this.indexCache.get(entry);
        if (entryIndex != null) {
            return entryIndex + 1;
        }
        return -1;
    }

    private void initialSort() {
        TreeSet<ClassFileEntry> inCpAll = new TreeSet<ClassFileEntry>(Comparator.comparingInt(arg0 -> ((ConstantPoolEntry)arg0).getGlobalIndex()));
        TreeSet<ClassFileEntry> cpUtf8sNotInCpAll = new TreeSet<ClassFileEntry>(Comparator.comparing(arg0 -> ((CPUTF8)arg0).underlyingString()));
        TreeSet<ClassFileEntry> cpClassesNotInCpAll = new TreeSet<ClassFileEntry>(Comparator.comparing(arg0 -> ((CPClass)arg0).getName()));
        for (ClassFileEntry entry2 : this.entries) {
            ConstantPoolEntry entry = (ConstantPoolEntry)entry2;
            if (entry.getGlobalIndex() == -1) {
                if (entry instanceof CPUTF8) {
                    cpUtf8sNotInCpAll.add(entry);
                    continue;
                }
                if (entry instanceof CPClass) {
                    cpClassesNotInCpAll.add(entry);
                    continue;
                }
                throw new Error("error");
            }
            inCpAll.add(entry);
        }
        this.entries.clear();
        this.entries.addAll(inCpAll);
        this.entries.addAll(cpUtf8sNotInCpAll);
        this.entries.addAll(cpClassesNotInCpAll);
    }

    public void resolve(Segment segment) {
        this.initialSort();
        this.sortClassPool();
        this.resolved = true;
        this.entries.forEach(entry -> entry.resolve(this));
        this.others.forEach(entry -> entry.resolve(this));
    }

    public int size() {
        return this.entries.size();
    }

    protected void sortClassPool() {
        ArrayList<ClassFileEntry> startOfPool = new ArrayList<ClassFileEntry>(this.entries.size());
        ArrayList<ClassFileEntry> finalSort = new ArrayList<ClassFileEntry>(this.entries.size());
        for (ClassFileEntry entry : this.entries) {
            if (this.mustStartClassPool.contains(entry)) {
                startOfPool.add(entry);
                continue;
            }
            finalSort.add(entry);
        }
        this.indexCache = new HashMap<ClassFileEntry, Integer>(this.entries.size());
        int index = 0;
        this.entries.clear();
        for (ClassFileEntry entry : startOfPool) {
            this.indexCache.put(entry, index);
            if (entry instanceof CPLong || entry instanceof CPDouble) {
                this.entries.add(entry);
                this.entries.add(entry);
                index += 2;
                continue;
            }
            this.entries.add(entry);
            ++index;
        }
        for (ClassFileEntry entry : finalSort) {
            this.indexCache.put(entry, index);
            if (entry instanceof CPLong || entry instanceof CPDouble) {
                this.entries.add(entry);
                this.entries.add(entry);
                index += 2;
                continue;
            }
            this.entries.add(entry);
            ++index;
        }
    }
}

