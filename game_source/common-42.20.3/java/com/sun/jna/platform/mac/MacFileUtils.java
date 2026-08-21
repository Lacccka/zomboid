/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.mac;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.platform.FileUtils;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.PointerByReference;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MacFileUtils
extends FileUtils {
    @Override
    public boolean hasTrash() {
        return true;
    }

    @Override
    public void moveToTrash(File ... files) throws IOException {
        ArrayList<String> failed = new ArrayList<String>();
        for (File src : files) {
            FileManager.FSRef fsref = new FileManager.FSRef();
            int status = FileManager.INSTANCE.FSPathMakeRefWithOptions(src.getAbsolutePath(), 1, fsref, null);
            if (status != 0) {
                failed.add(src + " (FSRef: " + status + ")");
                continue;
            }
            status = FileManager.INSTANCE.FSMoveObjectToTrashSync(fsref, null, 0);
            if (status == 0) continue;
            failed.add(src + " (" + status + ")");
        }
        if (failed.size() > 0) {
            throw new IOException("The following files could not be trashed: " + failed);
        }
    }

    public static interface FileManager
    extends Library {
        public static final FileManager INSTANCE = Native.load("CoreServices", FileManager.class);
        public static final int kFSFileOperationDefaultOptions = 0;
        public static final int kFSFileOperationsOverwrite = 1;
        public static final int kFSFileOperationsSkipSourcePermissionErrors = 2;
        public static final int kFSFileOperationsDoNotMoveAcrossVolumes = 4;
        public static final int kFSFileOperationsSkipPreflight = 8;
        public static final int kFSPathDefaultOptions = 0;
        public static final int kFSPathMakeRefDoNotFollowLeafSymlink = 1;

        public int FSRefMakePath(FSRef var1, byte[] var2, int var3);

        public int FSPathMakeRef(String var1, int var2, ByteByReference var3);

        public int FSPathMakeRefWithOptions(String var1, int var2, FSRef var3, ByteByReference var4);

        public int FSPathMoveObjectToTrashSync(String var1, PointerByReference var2, int var3);

        public int FSMoveObjectToTrashSync(FSRef var1, FSRef var2, int var3);

        @Structure.FieldOrder(value={"hidden"})
        public static class FSRef
        extends Structure {
            public byte[] hidden = new byte[80];
        }
    }
}

