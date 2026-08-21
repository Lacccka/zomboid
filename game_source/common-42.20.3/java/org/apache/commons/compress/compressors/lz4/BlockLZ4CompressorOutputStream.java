/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.compressors.lz4;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.lz77support.LZ77Compressor;
import org.apache.commons.compress.compressors.lz77support.Parameters;
import org.apache.commons.compress.utils.ByteUtils;

public class BlockLZ4CompressorOutputStream
extends CompressorOutputStream<OutputStream> {
    private static final int MIN_BACK_REFERENCE_LENGTH = 4;
    private static final int MIN_OFFSET_OF_LAST_BACK_REFERENCE = 12;
    private final LZ77Compressor compressor;
    private final byte[] oneByte = new byte[1];
    private boolean finished;
    private final Deque<Pair> pairs = new LinkedList<Pair>();
    private final Deque<byte[]> expandedBlocks = new LinkedList<byte[]>();

    public static Parameters.Builder createParameterBuilder() {
        int maxLen = 65535;
        return Parameters.builder(65536).withMinBackReferenceLength(4).withMaxBackReferenceLength(65535).withMaxOffset(65535).withMaxLiteralLength(65535);
    }

    public BlockLZ4CompressorOutputStream(OutputStream out) {
        this(out, BlockLZ4CompressorOutputStream.createParameterBuilder().build());
    }

    public BlockLZ4CompressorOutputStream(OutputStream out, Parameters params) {
        super(out);
        this.compressor = new LZ77Compressor(params, block -> {
            switch (block.getType()) {
                case LITERAL: {
                    this.addLiteralBlock((LZ77Compressor.LiteralBlock)block);
                    break;
                }
                case BACK_REFERENCE: {
                    this.addBackReference((LZ77Compressor.BackReference)block);
                    break;
                }
                case EOD: {
                    this.writeFinalLiteralBlock();
                }
            }
        });
    }

    private void addBackReference(LZ77Compressor.BackReference block) throws IOException {
        Pair last = this.writeBlocksAndReturnUnfinishedPair(block.getLength());
        last.setBackReference(block);
        this.recordBackReference(block);
        this.clearUnusedBlocksAndPairs();
    }

    private void addLiteralBlock(LZ77Compressor.LiteralBlock block) throws IOException {
        Pair last = this.writeBlocksAndReturnUnfinishedPair(block.getLength());
        this.recordLiteral(last.addLiteral(block));
        this.clearUnusedBlocksAndPairs();
    }

    private void clearUnusedBlocks() {
        int blockLengths = 0;
        int blocksToKeep = 0;
        for (byte[] b : this.expandedBlocks) {
            ++blocksToKeep;
            if ((blockLengths += b.length) < 65536) continue;
            break;
        }
        int size = this.expandedBlocks.size();
        for (int i = blocksToKeep; i < size; ++i) {
            this.expandedBlocks.removeLast();
        }
    }

    private void clearUnusedBlocksAndPairs() {
        this.clearUnusedBlocks();
        this.clearUnusedPairs();
    }

    private void clearUnusedPairs() {
        Pair p;
        int pairLengths = 0;
        int pairsToKeep = 0;
        Iterator<Pair> it = this.pairs.descendingIterator();
        while (it.hasNext()) {
            Pair p2 = it.next();
            ++pairsToKeep;
            if ((pairLengths += p2.length()) < 65536) continue;
            break;
        }
        int size = this.pairs.size();
        for (int i = pairsToKeep; i < size && (p = this.pairs.peekFirst()).hasBeenWritten(); ++i) {
            this.pairs.removeFirst();
        }
    }

    @Override
    public void close() throws IOException {
        try {
            this.finish();
        }
        finally {
            this.out.close();
        }
    }

    private byte[] expand(int offset, int length) {
        byte[] expanded = new byte[length];
        if (offset == 1) {
            byte[] block = this.expandedBlocks.peekFirst();
            byte b = block[block.length - 1];
            if (b != 0) {
                Arrays.fill(expanded, b);
            }
        } else {
            this.expandFromList(expanded, offset, length);
        }
        return expanded;
    }

    private void expandFromList(byte[] expanded, int offset, int length) {
        int offsetRemaining = offset;
        int lengthRemaining = length;
        int writeOffset = 0;
        while (lengthRemaining > 0) {
            int copyLen;
            int copyOffset;
            byte[] block = null;
            if (offsetRemaining > 0) {
                int blockOffset = 0;
                for (byte[] b : this.expandedBlocks) {
                    if (b.length + blockOffset >= offsetRemaining) {
                        block = b;
                        break;
                    }
                    blockOffset += b.length;
                }
                if (block == null) {
                    throw new IllegalStateException("Failed to find a block containing offset " + offset);
                }
                copyOffset = blockOffset + block.length - offsetRemaining;
                copyLen = Math.min(lengthRemaining, block.length - copyOffset);
            } else {
                block = expanded;
                copyOffset = -offsetRemaining;
                copyLen = Math.min(lengthRemaining, writeOffset + offsetRemaining);
            }
            System.arraycopy(block, copyOffset, expanded, writeOffset, copyLen);
            offsetRemaining -= copyLen;
            lengthRemaining -= copyLen;
            writeOffset += copyLen;
        }
    }

    public void finish() throws IOException {
        if (!this.finished) {
            this.compressor.finish();
            this.finished = true;
        }
    }

    public void prefill(byte[] data, int off, int len) {
        if (len > 0) {
            byte[] b = Arrays.copyOfRange(data, off, off + len);
            this.compressor.prefill(b);
            this.recordLiteral(b);
        }
    }

    private void recordBackReference(LZ77Compressor.BackReference block) {
        this.expandedBlocks.addFirst(this.expand(block.getOffset(), block.getLength()));
    }

    private void recordLiteral(byte[] b) {
        this.expandedBlocks.addFirst(b);
    }

    private void rewriteLastPairs() {
        int brLen;
        Pair p;
        LinkedList<Pair> lastPairs = new LinkedList<Pair>();
        LinkedList<Integer> pairLength = new LinkedList<Integer>();
        int offset = 0;
        Iterator<Pair> it = this.pairs.descendingIterator();
        while (it.hasNext() && !(p = it.next()).hasBeenWritten()) {
            int len = p.length();
            pairLength.addFirst(len);
            lastPairs.addFirst(p);
            if ((offset += len) < 12) continue;
            break;
        }
        lastPairs.forEach(this.pairs::remove);
        int lastPairsSize = lastPairs.size();
        int toExpand = 0;
        for (int i = 1; i < lastPairsSize; ++i) {
            toExpand += ((Integer)pairLength.get(i)).intValue();
        }
        Pair replacement = new Pair();
        if (toExpand > 0) {
            replacement.prependLiteral(this.expand(toExpand, toExpand));
        }
        Pair splitCandidate = (Pair)lastPairs.get(0);
        int stillNeeded = 12 - toExpand;
        int n = brLen = splitCandidate.hasBackReference() ? splitCandidate.backReferenceLength() : 0;
        if (splitCandidate.hasBackReference() && brLen >= 4 + stillNeeded) {
            replacement.prependLiteral(this.expand(toExpand + stillNeeded, stillNeeded));
            this.pairs.add(splitCandidate.splitWithNewBackReferenceLengthOf(brLen - stillNeeded));
        } else {
            if (splitCandidate.hasBackReference()) {
                replacement.prependLiteral(this.expand(toExpand + brLen, brLen));
            }
            splitCandidate.prependTo(replacement);
        }
        this.pairs.add(replacement);
    }

    @Override
    public void write(byte[] data, int off, int len) throws IOException {
        this.compressor.compress(data, off, len);
    }

    @Override
    public void write(int b) throws IOException {
        this.oneByte[0] = (byte)(b & 0xFF);
        this.write(this.oneByte);
    }

    private Pair writeBlocksAndReturnUnfinishedPair(int length) throws IOException {
        this.writeWritablePairs(length);
        Pair last = this.pairs.peekLast();
        if (last == null || last.hasBackReference()) {
            last = new Pair();
            this.pairs.addLast(last);
        }
        return last;
    }

    private void writeFinalLiteralBlock() throws IOException {
        this.rewriteLastPairs();
        for (Pair p : this.pairs) {
            if (p.hasBeenWritten()) continue;
            p.writeTo(this.out);
        }
        this.pairs.clear();
    }

    private void writeWritablePairs(int lengthOfBlocksAfterLastPair) throws IOException {
        Pair p2;
        int unwrittenLength = lengthOfBlocksAfterLastPair;
        Iterator<Pair> it = this.pairs.descendingIterator();
        while (it.hasNext() && !(p2 = it.next()).hasBeenWritten()) {
            unwrittenLength += p2.length();
        }
        for (Pair p2 : this.pairs) {
            if (p2.hasBeenWritten()) continue;
            if (!p2.canBeWritten(unwrittenLength -= p2.length())) break;
            p2.writeTo(this.out);
        }
    }

    static final class Pair {
        private final Deque<byte[]> literals = new LinkedList<byte[]>();
        private int literalLength;
        private int brOffset;
        private int brLength;
        private boolean written;

        Pair() {
        }

        private static int lengths(int litLength, int brLength) {
            int l = Math.min(litLength, 15);
            int br = brLength < 4 ? 0 : (brLength < 19 ? brLength - 4 : 15);
            return l << 4 | br;
        }

        private static void writeLength(int length, OutputStream out) throws IOException {
            while (length >= 255) {
                out.write(255);
                length -= 255;
            }
            out.write(length);
        }

        byte[] addLiteral(LZ77Compressor.LiteralBlock block) {
            byte[] copy = Arrays.copyOfRange(block.getData(), block.getOffset(), block.getOffset() + block.getLength());
            this.literals.add(copy);
            this.literalLength += copy.length;
            return copy;
        }

        private int backReferenceLength() {
            return this.brLength;
        }

        boolean canBeWritten(int lengthOfBlocksAfterThisPair) {
            return this.hasBackReference() && lengthOfBlocksAfterThisPair >= 16;
        }

        boolean hasBackReference() {
            return this.brOffset > 0;
        }

        private boolean hasBeenWritten() {
            return this.written;
        }

        int length() {
            return this.literalLength() + this.brLength;
        }

        private int literalLength() {
            if (this.literalLength != 0) {
                return this.literalLength;
            }
            int length = 0;
            for (byte[] b : this.literals) {
                length += b.length;
            }
            this.literalLength = length;
            return this.literalLength;
        }

        private void prependLiteral(byte[] data) {
            this.literals.addFirst(data);
            this.literalLength += data.length;
        }

        private void prependTo(Pair other) {
            Iterator<byte[]> listBackwards = this.literals.descendingIterator();
            while (listBackwards.hasNext()) {
                other.prependLiteral(listBackwards.next());
            }
        }

        void setBackReference(LZ77Compressor.BackReference block) {
            if (this.hasBackReference()) {
                throw new IllegalStateException();
            }
            this.brOffset = block.getOffset();
            this.brLength = block.getLength();
        }

        private Pair splitWithNewBackReferenceLengthOf(int newBackReferenceLength) {
            Pair p = new Pair();
            p.literals.addAll(this.literals);
            p.brOffset = this.brOffset;
            p.brLength = newBackReferenceLength;
            return p;
        }

        void writeTo(OutputStream out) throws IOException {
            int litLength = this.literalLength();
            out.write(Pair.lengths(litLength, this.brLength));
            if (litLength >= 15) {
                Pair.writeLength(litLength - 15, out);
            }
            for (byte[] b : this.literals) {
                out.write(b);
            }
            if (this.hasBackReference()) {
                ByteUtils.toLittleEndian(out, (long)this.brOffset, 2);
                if (this.brLength - 4 >= 15) {
                    Pair.writeLength(this.brLength - 4 - 15, out);
                }
            }
            this.written = true;
        }
    }
}

