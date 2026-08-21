/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.changes;

import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.changes.Change;

public final class ChangeSet<E extends ArchiveEntry> {
    private final Set<Change<E>> changes = new LinkedHashSet<Change<E>>();

    public void add(E entry, InputStream input) {
        this.add(entry, input, true);
    }

    public void add(E entry, InputStream input, boolean replace) {
        this.addAddition(new Change<E>(entry, input, replace));
    }

    private void addAddition(Change<E> addChange) {
        if (Change.ChangeType.ADD != addChange.getType() || addChange.getInputStream() == null) {
            return;
        }
        if (!this.changes.isEmpty()) {
            Iterator<Change<E>> it = this.changes.iterator();
            while (it.hasNext()) {
                E entry;
                Change<E> change = it.next();
                if (change.getType() != Change.ChangeType.ADD || change.getEntry() == null || !(entry = change.getEntry()).equals(addChange.getEntry())) continue;
                if (addChange.isReplaceMode()) {
                    it.remove();
                    this.changes.add(addChange);
                }
                return;
            }
        }
        this.changes.add(addChange);
    }

    private void addDeletion(Change<E> deleteChange) {
        if (Change.ChangeType.DELETE != deleteChange.getType() && Change.ChangeType.DELETE_DIR != deleteChange.getType() || deleteChange.getTargetFileName() == null) {
            return;
        }
        String source2 = deleteChange.getTargetFileName();
        Pattern pattern = Pattern.compile(source2 + "/.*");
        if (source2 != null && !this.changes.isEmpty()) {
            Iterator<Change<E>> it = this.changes.iterator();
            while (it.hasNext()) {
                String target;
                Change<E> change = it.next();
                if (change.getType() != Change.ChangeType.ADD || change.getEntry() == null || (target = change.getEntry().getName()) == null || (Change.ChangeType.DELETE != deleteChange.getType() || !source2.equals(target)) && (Change.ChangeType.DELETE_DIR != deleteChange.getType() || !pattern.matcher(target).matches())) continue;
                it.remove();
            }
        }
        this.changes.add(deleteChange);
    }

    public void delete(String fileName) {
        this.addDeletion(new Change(fileName, Change.ChangeType.DELETE));
    }

    public void deleteDir(String dirName) {
        this.addDeletion(new Change(dirName, Change.ChangeType.DELETE_DIR));
    }

    Set<Change<E>> getChanges() {
        return new LinkedHashSet<Change<E>>(this.changes);
    }
}

