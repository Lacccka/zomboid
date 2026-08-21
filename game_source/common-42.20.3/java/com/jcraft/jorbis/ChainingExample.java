/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jorbis;

import com.jcraft.jorbis.Comment;
import com.jcraft.jorbis.Info;
import com.jcraft.jorbis.VorbisFile;
import zombie.debug.DebugLog;
import zombie.debug.DebugType;

class ChainingExample {
    ChainingExample() {
    }

    static void main(String[] arg) {
        VorbisFile ov = null;
        try {
            ov = arg.length > 0 ? new VorbisFile(arg[0]) : new VorbisFile(System.in, null, -1);
        }
        catch (Exception e) {
            System.err.println(e);
            return;
        }
        if (ov.seekable()) {
            DebugLog.log("Input bitstream contained " + ov.streams() + " logical bitstream section(s).");
            DebugLog.log("Total bitstream playing time: " + ov.time_total(-1) + " seconds\n");
        } else {
            DebugLog.log("Standard input was not seekable.");
            DebugLog.log("First logical bitstream information:\n");
        }
        for (int i = 0; i < ov.streams(); ++i) {
            Info vi = ov.getInfo(i);
            DebugLog.log("\tlogical bitstream section " + (i + 1) + " information:");
            DebugLog.log("\t\t" + vi.rate + "Hz " + vi.channels + " channels bitrate " + ov.bitrate(i) / 1000 + "kbps serial number=" + ov.serialnumber(i));
            System.out.print("\t\tcompressed length: " + ov.raw_total(i) + " bytes ");
            DebugLog.log(" play time: " + ov.time_total(i) + "s");
            Comment vc = ov.getComment(i);
            DebugType.General.println(vc);
        }
    }
}

