/*
 * Decompiled with CFR 0.152.
 */
package com.fasterxml.jackson.databind.node;

import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import com.fasterxml.jackson.databind.node.InternalNodeMapper;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

class NodeSerialization
implements Serializable,
Externalizable {
    protected static final int LONGEST_EAGER_ALLOC = 100000;
    private static final long serialVersionUID = 1L;
    public byte[] json;

    public NodeSerialization() {
    }

    public NodeSerialization(byte[] b) {
        this.json = b;
    }

    protected Object readResolve() {
        try {
            return InternalNodeMapper.bytesToNode(this.json);
        }
        catch (IOException e) {
            throw new IllegalArgumentException("Failed to JDK deserialize `JsonNode` value: " + e.getMessage(), e);
        }
    }

    public static NodeSerialization from(Object o) {
        try {
            return new NodeSerialization(InternalNodeMapper.valueToBytes(o));
        }
        catch (IOException e) {
            throw new IllegalArgumentException("Failed to JDK serialize `" + o.getClass().getSimpleName() + "` value: " + e.getMessage(), e);
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(this.json.length);
        out.write(this.json);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException {
        int len = in.readInt();
        this.json = this._read(in, len);
    }

    private byte[] _read(ObjectInput in, int expLen) throws IOException {
        if (expLen <= 100000) {
            byte[] result = new byte[expLen];
            in.readFully(result, 0, expLen);
            return result;
        }
        try (ByteArrayBuilder bb = new ByteArrayBuilder(100000);){
            byte[] buffer = bb.resetAndGetFirstSegment();
            int outOffset = 0;
            while (true) {
                int toRead = Math.min(buffer.length - outOffset, expLen);
                in.readFully(buffer, 0, toRead);
                outOffset += toRead;
                if ((expLen -= toRead) == 0) {
                    byte[] byArray = bb.completeAndCoalesce(outOffset);
                    return byArray;
                }
                if (outOffset != buffer.length) continue;
                buffer = bb.finishCurrentSegment();
                outOffset = 0;
            }
        }
    }
}

