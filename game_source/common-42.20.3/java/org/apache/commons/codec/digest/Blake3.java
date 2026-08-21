/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.codec.digest;

import java.util.Arrays;
import java.util.Objects;

public final class Blake3 {
    private static final int BLOCK_LEN = 64;
    private static final int BLOCK_INTS = 16;
    private static final int KEY_LEN = 32;
    private static final int KEY_INTS = 8;
    private static final int OUT_LEN = 32;
    private static final int CHUNK_LEN = 1024;
    private static final int CHAINING_VALUE_INTS = 8;
    private static final int[] IV = new int[]{1779033703, -1150833019, 1013904242, -1521486534, 1359893119, -1694144372, 528734635, 1541459225};
    private static final int CHUNK_START = 1;
    private static final int CHUNK_END = 2;
    private static final int PARENT = 4;
    private static final int ROOT = 8;
    private static final int KEYED_HASH = 16;
    private static final int DERIVE_KEY_CONTEXT = 32;
    private static final int DERIVE_KEY_MATERIAL = 64;
    private static final byte[][] MSG_SCHEDULE = new byte[][]{{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15}, {2, 6, 3, 10, 7, 0, 4, 13, 1, 11, 12, 5, 9, 14, 15, 8}, {3, 4, 10, 12, 13, 2, 7, 14, 6, 5, 9, 0, 11, 15, 8, 1}, {10, 7, 12, 9, 14, 3, 13, 15, 4, 0, 11, 2, 5, 8, 1, 6}, {12, 13, 9, 11, 15, 10, 14, 8, 7, 2, 5, 3, 0, 1, 6, 4}, {9, 14, 11, 5, 8, 12, 15, 1, 13, 3, 0, 10, 2, 6, 4, 7}, {11, 15, 5, 0, 1, 9, 8, 6, 14, 10, 2, 12, 3, 4, 7, 13}};
    private final EngineState engineState;

    private static void checkBufferArgs(byte[] buffer, int offset, int length) {
        Objects.requireNonNull(buffer);
        if (offset < 0) {
            throw new IndexOutOfBoundsException("Offset must be non-negative");
        }
        if (length < 0) {
            throw new IndexOutOfBoundsException("Length must be non-negative");
        }
        int bufferLength = buffer.length;
        if (offset > bufferLength - length) {
            throw new IndexOutOfBoundsException("Offset " + offset + " and length " + length + " out of bounds with buffer length " + bufferLength);
        }
    }

    private static int[] compress(int[] chainingValue, int[] blockWords, int blockLength, long counter, int flags) {
        int i;
        int[] state = Arrays.copyOf(chainingValue, 16);
        System.arraycopy(IV, 0, state, 8, 4);
        state[12] = (int)counter;
        state[13] = (int)(counter >> 32);
        state[14] = blockLength;
        state[15] = flags;
        for (i = 0; i < 7; ++i) {
            byte[] schedule2 = MSG_SCHEDULE[i];
            Blake3.round(state, blockWords, schedule2);
        }
        for (i = 0; i < state.length / 2; ++i) {
            int n = i;
            state[n] = state[n] ^ state[i + 8];
            int n2 = i + 8;
            state[n2] = state[n2] ^ chainingValue[i];
        }
        return state;
    }

    private static void g(int[] state, int a, int b, int c, int d, int mx, int my) {
        int n = a;
        state[n] = state[n] + (state[b] + mx);
        state[d] = Integer.rotateRight(state[d] ^ state[a], 16);
        int n2 = c;
        state[n2] = state[n2] + state[d];
        state[b] = Integer.rotateRight(state[b] ^ state[c], 12);
        int n3 = a;
        state[n3] = state[n3] + (state[b] + my);
        state[d] = Integer.rotateRight(state[d] ^ state[a], 8);
        int n4 = c;
        state[n4] = state[n4] + state[d];
        state[b] = Integer.rotateRight(state[b] ^ state[c], 7);
    }

    public static byte[] hash(byte[] data) {
        return Blake3.initHash().update(data).doFinalize(32);
    }

    public static Blake3 initHash() {
        return new Blake3(IV, 0);
    }

    public static Blake3 initKeyDerivationFunction(byte[] kdfContext) {
        Objects.requireNonNull(kdfContext);
        EngineState kdf = new EngineState(IV, 32);
        kdf.inputData(kdfContext, 0, kdfContext.length);
        byte[] key = new byte[32];
        kdf.outputHash(key, 0, key.length);
        return new Blake3(Blake3.unpackInts(key, 8), 64);
    }

    public static Blake3 initKeyedHash(byte[] key) {
        Objects.requireNonNull(key);
        if (key.length != 32) {
            throw new IllegalArgumentException("Blake3 keys must be 32 bytes");
        }
        return new Blake3(Blake3.unpackInts(key, 8), 16);
    }

    public static byte[] keyedHash(byte[] key, byte[] data) {
        return Blake3.initKeyedHash(key).update(data).doFinalize(32);
    }

    private static void packInt(int value, byte[] dst, int off, int len) {
        for (int i = 0; i < len; ++i) {
            dst[off + i] = (byte)(value >>> i * 8);
        }
    }

    private static int[] parentChainingValue(int[] leftChildCV, int[] rightChildCV, int[] key, int flags) {
        return Blake3.parentOutput(leftChildCV, rightChildCV, key, flags).chainingValue();
    }

    private static Output parentOutput(int[] leftChildCV, int[] rightChildCV, int[] key, int flags) {
        int[] blockWords = Arrays.copyOf(leftChildCV, 16);
        System.arraycopy(rightChildCV, 0, blockWords, 8, 8);
        return new Output((int[])key.clone(), blockWords, 0L, 64, flags | 4);
    }

    private static void round(int[] state, int[] msg, byte[] schedule2) {
        Blake3.g(state, 0, 4, 8, 12, msg[schedule2[0]], msg[schedule2[1]]);
        Blake3.g(state, 1, 5, 9, 13, msg[schedule2[2]], msg[schedule2[3]]);
        Blake3.g(state, 2, 6, 10, 14, msg[schedule2[4]], msg[schedule2[5]]);
        Blake3.g(state, 3, 7, 11, 15, msg[schedule2[6]], msg[schedule2[7]]);
        Blake3.g(state, 0, 5, 10, 15, msg[schedule2[8]], msg[schedule2[9]]);
        Blake3.g(state, 1, 6, 11, 12, msg[schedule2[10]], msg[schedule2[11]]);
        Blake3.g(state, 2, 7, 8, 13, msg[schedule2[12]], msg[schedule2[13]]);
        Blake3.g(state, 3, 4, 9, 14, msg[schedule2[14]], msg[schedule2[15]]);
    }

    private static int unpackInt(byte[] buf, int off) {
        return buf[off] & 0xFF | (buf[off + 1] & 0xFF) << 8 | (buf[off + 2] & 0xFF) << 16 | (buf[off + 3] & 0xFF) << 24;
    }

    private static int[] unpackInts(byte[] buf, int nrInts) {
        int[] values2 = new int[nrInts];
        int i = 0;
        int off = 0;
        while (i < nrInts) {
            values2[i] = Blake3.unpackInt(buf, off);
            ++i;
            off += 4;
        }
        return values2;
    }

    private Blake3(int[] key, int flags) {
        this.engineState = new EngineState(key, flags);
    }

    public Blake3 doFinalize(byte[] out) {
        return this.doFinalize(out, 0, out.length);
    }

    public Blake3 doFinalize(byte[] out, int offset, int length) {
        Blake3.checkBufferArgs(out, offset, length);
        this.engineState.outputHash(out, offset, length);
        return this;
    }

    public byte[] doFinalize(int nrBytes) {
        if (nrBytes < 0) {
            throw new IllegalArgumentException("Requested bytes must be non-negative");
        }
        byte[] hash = new byte[nrBytes];
        this.doFinalize(hash);
        return hash;
    }

    public Blake3 reset() {
        this.engineState.reset();
        return this;
    }

    public Blake3 update(byte[] in) {
        return this.update(in, 0, in.length);
    }

    public Blake3 update(byte[] in, int offset, int length) {
        Blake3.checkBufferArgs(in, offset, length);
        this.engineState.inputData(in, offset, length);
        return this;
    }

    private static final class Output {
        private final int[] inputChainingValue;
        private final int[] blockWords;
        private final long counter;
        private final int blockLength;
        private final int flags;

        private Output(int[] inputChainingValue, int[] blockWords, long counter, int blockLength, int flags) {
            this.inputChainingValue = inputChainingValue;
            this.blockWords = blockWords;
            this.counter = counter;
            this.blockLength = blockLength;
            this.flags = flags;
        }

        private int[] chainingValue() {
            return Arrays.copyOf(Blake3.compress(this.inputChainingValue, this.blockWords, this.blockLength, this.counter, this.flags), 8);
        }

        private void rootOutputBytes(byte[] out, int offset, int length) {
            int outputBlockCounter = 0;
            while (length > 0) {
                int chunkLength = Math.min(64, length);
                length -= chunkLength;
                int[] words = Blake3.compress(this.inputChainingValue, this.blockWords, this.blockLength, outputBlockCounter++, this.flags | 8);
                int wordCounter = 0;
                while (chunkLength > 0) {
                    int wordLength = Math.min(4, chunkLength);
                    Blake3.packInt(words[wordCounter++], out, offset, wordLength);
                    offset += wordLength;
                    chunkLength -= wordLength;
                }
            }
        }
    }

    private static final class EngineState {
        private final int[] key;
        private final int flags;
        private final int[][] cvStack = new int[54][];
        private int stackLen;
        private ChunkState state;

        private EngineState(int[] key, int flags) {
            this.key = key;
            this.flags = flags;
            this.state = new ChunkState(key, 0L, flags);
        }

        private void addChunkCV(int[] firstCV, long totalChunks) {
            int[] newCV = firstCV;
            long chunkCounter = totalChunks;
            while ((chunkCounter & 1L) == 0L) {
                newCV = Blake3.parentChainingValue(this.popCV(), newCV, this.key, this.flags);
                chunkCounter >>= 1;
            }
            this.pushCV(newCV);
        }

        private void inputData(byte[] in, int offset, int length) {
            while (length > 0) {
                if (this.state.length() == 1024) {
                    int[] chunkCV = this.state.output().chainingValue();
                    long totalChunks = this.state.chunkCounter + 1L;
                    this.addChunkCV(chunkCV, totalChunks);
                    this.state = new ChunkState(this.key, totalChunks, this.flags);
                }
                int want = 1024 - this.state.length();
                int take = Math.min(want, length);
                this.state.update(in, offset, take);
                offset += take;
                length -= take;
            }
        }

        private void outputHash(byte[] out, int offset, int length) {
            Output output = this.state.output();
            int parentNodesRemaining = this.stackLen;
            while (parentNodesRemaining-- > 0) {
                int[] parentCV = this.cvStack[parentNodesRemaining];
                output = Blake3.parentOutput(parentCV, output.chainingValue(), this.key, this.flags);
            }
            output.rootOutputBytes(out, offset, length);
        }

        private int[] popCV() {
            return this.cvStack[--this.stackLen];
        }

        private void pushCV(int[] cv) {
            this.cvStack[this.stackLen++] = cv;
        }

        private void reset() {
            this.stackLen = 0;
            Arrays.fill((Object[])this.cvStack, null);
            this.state = new ChunkState(this.key, 0L, this.flags);
        }
    }

    private static final class ChunkState {
        private int[] chainingValue;
        private final long chunkCounter;
        private final int flags;
        private final byte[] block = new byte[64];
        private int blockLength;
        private int blocksCompressed;

        private ChunkState(int[] key, long chunkCounter, int flags) {
            this.chainingValue = key;
            this.chunkCounter = chunkCounter;
            this.flags = flags;
        }

        private int length() {
            return 64 * this.blocksCompressed + this.blockLength;
        }

        private Output output() {
            int[] blockWords = Blake3.unpackInts(this.block, 16);
            int outputFlags = this.flags | this.startFlag() | 2;
            return new Output(this.chainingValue, blockWords, this.chunkCounter, this.blockLength, outputFlags);
        }

        private int startFlag() {
            return this.blocksCompressed == 0 ? 1 : 0;
        }

        private void update(byte[] input, int offset, int length) {
            while (length > 0) {
                if (this.blockLength == 64) {
                    int[] blockWords = Blake3.unpackInts(this.block, 16);
                    this.chainingValue = Arrays.copyOf(Blake3.compress(this.chainingValue, blockWords, 64, this.chunkCounter, this.flags | this.startFlag()), 8);
                    ++this.blocksCompressed;
                    this.blockLength = 0;
                    Arrays.fill(this.block, (byte)0);
                }
                int want = 64 - this.blockLength;
                int take = Math.min(want, length);
                System.arraycopy(input, offset, this.block, this.blockLength, take);
                this.blockLength += take;
                offset += take;
                length -= take;
            }
        }
    }
}

