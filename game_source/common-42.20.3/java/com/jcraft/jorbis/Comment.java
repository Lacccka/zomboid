/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jorbis;

import com.jcraft.jogg.Buffer;
import com.jcraft.jogg.Packet;

public class Comment {
    private static final byte[] _vorbis = "vorbis".getBytes();
    private static final byte[] _vendor = "Xiphophorus libVorbis I 20000508".getBytes();
    private static final int OV_EIMPL = -130;
    public int[] commentLengths;
    public int comments;
    public byte[][] userComments;
    public byte[] vendor;

    static boolean tagcompare(byte[] s1, byte[] s2, int n) {
        for (int c = 0; c < n; ++c) {
            byte u1 = s1[c];
            byte u2 = s2[c];
            if (90 >= u1 && u1 >= 65) {
                u1 = (byte)(u1 - 65 + 97);
            }
            if (90 >= u2 && u2 >= 65) {
                u2 = (byte)(u2 - 65 + 97);
            }
            if (u1 == u2) continue;
            return false;
        }
        return true;
    }

    public void add(String comment) {
        this.add(comment.getBytes());
    }

    public void add_tag(String tag, String contents) {
        if (contents == null) {
            contents = "";
        }
        this.add(tag + "=" + contents);
    }

    public String getComment(int i) {
        if (this.comments <= i) {
            return null;
        }
        return new String(this.userComments[i], 0, this.userComments[i].length - 1);
    }

    public String getVendor() {
        return new String(this.vendor, 0, this.vendor.length - 1);
    }

    public int header_out(Packet op) {
        Buffer opb = new Buffer();
        opb.writeinit();
        if (this.pack(opb) != 0) {
            return -130;
        }
        op.packetBase = new byte[opb.bytes()];
        op.packet = 0;
        op.bytes = opb.bytes();
        System.arraycopy(opb.buffer(), 0, op.packetBase, 0, op.bytes);
        op.beginOfStream = 0;
        op.endOfStream = 0;
        op.granulepos = 0L;
        return 0;
    }

    public void init() {
        this.userComments = null;
        this.comments = 0;
        this.vendor = null;
    }

    public String query(String tag) {
        return this.query(tag, 0);
    }

    public String query(String tag, int count) {
        int foo = this.query(tag.getBytes(), count);
        if (foo == -1) {
            return null;
        }
        byte[] comment = this.userComments[foo];
        for (int i = 0; i < this.commentLengths[foo]; ++i) {
            if (comment[i] != 61) continue;
            return new String(comment, i + 1, this.commentLengths[foo] - (i + 1));
        }
        return null;
    }

    public String toString() {
        String foo = "Vendor: " + new String(this.vendor, 0, this.vendor.length - 1);
        for (int i = 0; i < this.comments; ++i) {
            foo = foo + "\nComment: " + new String(this.userComments[i], 0, this.userComments[i].length - 1);
        }
        foo = foo + "\n";
        return foo;
    }

    void clear() {
        for (int i = 0; i < this.comments; ++i) {
            this.userComments[i] = null;
        }
        this.userComments = null;
        this.vendor = null;
    }

    int pack(Buffer opb) {
        opb.write(3, 8);
        opb.write(_vorbis);
        opb.write(_vendor.length, 32);
        opb.write(_vendor);
        opb.write(this.comments, 32);
        if (this.comments != 0) {
            for (int i = 0; i < this.comments; ++i) {
                if (this.userComments[i] != null) {
                    opb.write(this.commentLengths[i], 32);
                    opb.write(this.userComments[i]);
                    continue;
                }
                opb.write(0, 32);
            }
        }
        opb.write(1, 1);
        return 0;
    }

    int unpack(Buffer opb) {
        int vendorlen = opb.read(32);
        if (vendorlen < 0) {
            this.clear();
            return -1;
        }
        this.vendor = new byte[vendorlen + 1];
        opb.read(this.vendor, vendorlen);
        this.comments = opb.read(32);
        if (this.comments < 0) {
            this.clear();
            return -1;
        }
        this.userComments = new byte[this.comments + 1][];
        this.commentLengths = new int[this.comments + 1];
        for (int i = 0; i < this.comments; ++i) {
            int len = opb.read(32);
            if (len < 0) {
                this.clear();
                return -1;
            }
            this.commentLengths[i] = len;
            this.userComments[i] = new byte[len + 1];
            opb.read(this.userComments[i], len);
        }
        if (opb.read(1) != 1) {
            this.clear();
            return -1;
        }
        return 0;
    }

    private void add(byte[] comment) {
        byte[][] foo = new byte[this.comments + 2][];
        if (this.userComments != null) {
            System.arraycopy(this.userComments, 0, foo, 0, this.comments);
        }
        this.userComments = foo;
        int[] goo = new int[this.comments + 2];
        if (this.commentLengths != null) {
            System.arraycopy(this.commentLengths, 0, goo, 0, this.comments);
        }
        this.commentLengths = goo;
        byte[] bar = new byte[comment.length + 1];
        System.arraycopy(comment, 0, bar, 0, comment.length);
        this.userComments[this.comments] = bar;
        this.commentLengths[this.comments] = comment.length;
        ++this.comments;
        this.userComments[this.comments] = null;
    }

    private int query(byte[] tag, int count) {
        int i = 0;
        int found = 0;
        int fulltaglen = tag.length + 1;
        byte[] fulltag = new byte[fulltaglen];
        System.arraycopy(tag, 0, fulltag, 0, tag.length);
        fulltag[tag.length] = 61;
        for (i = 0; i < this.comments; ++i) {
            if (!Comment.tagcompare(this.userComments[i], fulltag, fulltaglen)) continue;
            if (count == found) {
                return i;
            }
            ++found;
        }
        return -1;
    }
}

