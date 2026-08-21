/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.changes;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.changes.Change;
import org.apache.commons.compress.changes.ChangeSet;
import org.apache.commons.compress.changes.ChangeSetResults;
import org.apache.commons.io.IOUtils;

public class ChangeSetPerformer<I extends ArchiveInputStream<E>, O extends ArchiveOutputStream<E>, E extends ArchiveEntry> {
    private final Set<Change<E>> changes;

    public ChangeSetPerformer(ChangeSet<E> changeSet) {
        this.changes = changeSet.getChanges();
    }

    private void copyStream(InputStream inputStream2, O outputStream2, E archiveEntry) throws IOException {
        ((ArchiveOutputStream)outputStream2).putArchiveEntry(archiveEntry);
        IOUtils.copy(inputStream2, outputStream2);
        ((ArchiveOutputStream)outputStream2).closeArchiveEntry();
    }

    private boolean isDeletedLater(Set<Change<E>> workingSet, E entry) {
        String source2 = entry.getName();
        if (!workingSet.isEmpty()) {
            for (Change<E> change : workingSet) {
                Change.ChangeType type = change.getType();
                String target = change.getTargetFileName();
                if (type == Change.ChangeType.DELETE && source2.equals(target)) {
                    return true;
                }
                if (type != Change.ChangeType.DELETE_DIR || !source2.startsWith(target + "/")) continue;
                return true;
            }
        }
        return false;
    }

    private ChangeSetResults perform(ArchiveEntryIterator<E> entryIterator, O outputStream2) throws IOException {
        InputStream inputStream2;
        ChangeSetResults results = new ChangeSetResults();
        LinkedHashSet<Change<E>> workingSet = new LinkedHashSet<Change<E>>(this.changes);
        Iterator it = workingSet.iterator();
        while (it.hasNext()) {
            Change change = (Change)it.next();
            if (change.getType() != Change.ChangeType.ADD || !change.isReplaceMode()) continue;
            inputStream2 = change.getInputStream();
            this.copyStream(inputStream2, outputStream2, change.getEntry());
            it.remove();
            results.addedFromChangeSet(change.getEntry().getName());
        }
        while (entryIterator.hasNext()) {
            E entry = entryIterator.next();
            boolean copy = true;
            Iterator it2 = workingSet.iterator();
            while (it2.hasNext()) {
                Change change = (Change)it2.next();
                Change.ChangeType type = change.getType();
                String name = entry.getName();
                if (type == Change.ChangeType.DELETE && name != null) {
                    if (!name.equals(change.getTargetFileName())) continue;
                    copy = false;
                    it2.remove();
                    results.deleted(name);
                    break;
                }
                if (type != Change.ChangeType.DELETE_DIR || name == null || !name.startsWith(change.getTargetFileName() + "/")) continue;
                copy = false;
                results.deleted(name);
                break;
            }
            if (!copy || this.isDeletedLater(workingSet, entry) || results.hasBeenAdded(entry.getName())) continue;
            inputStream2 = entryIterator.getInputStream();
            this.copyStream(inputStream2, outputStream2, entry);
            results.addedFromStream(entry.getName());
        }
        it = workingSet.iterator();
        while (it.hasNext()) {
            Change change = (Change)it.next();
            if (change.getType() != Change.ChangeType.ADD || change.isReplaceMode() || results.hasBeenAdded(change.getEntry().getName())) continue;
            InputStream input = change.getInputStream();
            this.copyStream(input, outputStream2, change.getEntry());
            it.remove();
            results.addedFromChangeSet(change.getEntry().getName());
        }
        ((ArchiveOutputStream)outputStream2).finish();
        return results;
    }

    public ChangeSetResults perform(I inputStream2, O outputStream2) throws IOException {
        return this.perform((I)new ArchiveInputStreamIterator(inputStream2), outputStream2);
    }

    public ChangeSetResults perform(ZipFile zipFile, O outputStream2) throws IOException {
        ZipFileIterator entryIterator = new ZipFileIterator(zipFile);
        return this.perform((I)entryIterator, outputStream2);
    }

    private static interface ArchiveEntryIterator<E extends ArchiveEntry> {
        public InputStream getInputStream() throws IOException;

        public boolean hasNext() throws IOException;

        public E next();
    }

    private static final class ArchiveInputStreamIterator<E extends ArchiveEntry>
    implements ArchiveEntryIterator<E> {
        private final ArchiveInputStream<E> inputStream;
        private E next;

        ArchiveInputStreamIterator(ArchiveInputStream<E> inputStream2) {
            this.inputStream = inputStream2;
        }

        @Override
        public InputStream getInputStream() {
            return this.inputStream;
        }

        @Override
        public boolean hasNext() throws IOException {
            this.next = this.inputStream.getNextEntry();
            return this.next != null;
        }

        @Override
        public E next() {
            return this.next;
        }
    }

    private static final class ZipFileIterator
    implements ArchiveEntryIterator<ZipArchiveEntry> {
        private final ZipFile zipFile;
        private final Enumeration<ZipArchiveEntry> nestedEnumeration;
        private ZipArchiveEntry currentEntry;

        ZipFileIterator(ZipFile zipFile) {
            this.zipFile = zipFile;
            this.nestedEnumeration = zipFile.getEntriesInPhysicalOrder();
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return this.zipFile.getInputStream(this.currentEntry);
        }

        @Override
        public boolean hasNext() {
            return this.nestedEnumeration.hasMoreElements();
        }

        @Override
        public ZipArchiveEntry next() {
            this.currentEntry = this.nestedEnumeration.nextElement();
            return this.currentEntry;
        }
    }
}

