/*
 * Decompiled with CFR 0.152.
 */
package oshi.ffm.mac;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import oshi.ffm.mac.CoreFoundation;
import oshi.ffm.mac.DiskArbitrationFunctions;
import oshi.ffm.mac.IOKit;

public interface DiskArbitration {

    public static class DADiskRef
    extends CoreFoundation.CFTypeRef {
        public DADiskRef(MemorySegment segment) {
            super(segment);
        }

        public static DADiskRef createFromBSDName(CoreFoundation.CFAllocatorRef allocator, DASessionRef session, String bsdName) {
            try (Arena arena = Arena.ofConfined();){
                MemorySegment allocSeg = allocator != null ? allocator.segment() : MemorySegment.NULL;
                MemorySegment sessionSeg = session != null ? session.segment() : MemorySegment.NULL;
                MemorySegment bsdNameSeg = arena.allocateFrom(bsdName);
                MemorySegment diskSeg = DiskArbitrationFunctions.DADiskCreateFromBSDName(allocSeg, sessionSeg, bsdNameSeg);
                DADiskRef dADiskRef = new DADiskRef(diskSeg);
                return dADiskRef;
            }
        }

        public static DADiskRef createFromIOMedia(CoreFoundation.CFAllocatorRef allocator, DASessionRef session, IOKit.IOObject media) {
            MemorySegment allocSeg = allocator != null ? allocator.segment() : MemorySegment.NULL;
            MemorySegment sessionSeg = session != null ? session.segment() : MemorySegment.NULL;
            MemorySegment mediaSeg = media != null ? media.segment() : MemorySegment.NULL;
            MemorySegment diskSeg = DiskArbitrationFunctions.DADiskCreateFromIOMedia(allocSeg, sessionSeg, mediaSeg);
            return new DADiskRef(diskSeg);
        }

        public CoreFoundation.CFDictionaryRef copyDescription() {
            if (this.isNull()) {
                return new CoreFoundation.CFDictionaryRef(MemorySegment.NULL);
            }
            MemorySegment dictSeg = DiskArbitrationFunctions.DADiskCopyDescription(this.segment());
            return new CoreFoundation.CFDictionaryRef(dictSeg);
        }

        public String getBSDName() {
            if (this.isNull()) {
                return null;
            }
            MemorySegment nameSeg = DiskArbitrationFunctions.DADiskGetBSDName(this.segment());
            if (nameSeg.equals(MemorySegment.NULL)) {
                return null;
            }
            return nameSeg.getString(0L);
        }
    }

    public static class DASessionRef
    extends CoreFoundation.CFTypeRef {
        public DASessionRef(MemorySegment segment) {
            super(segment);
        }

        public static DASessionRef create(CoreFoundation.CFAllocatorRef allocator) {
            MemorySegment allocSeg = allocator != null ? allocator.segment() : MemorySegment.NULL;
            MemorySegment sessionSeg = DiskArbitrationFunctions.DASessionCreate(allocSeg);
            return new DASessionRef(sessionSeg);
        }
    }
}

