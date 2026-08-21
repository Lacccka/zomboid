/*
 * Decompiled with CFR 0.152.
 */
package org.uncommons.maths.random;

import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;
import org.uncommons.maths.binary.BinaryUtils;
import org.uncommons.maths.random.DefaultSeedGenerator;
import org.uncommons.maths.random.RepeatableRNG;
import org.uncommons.maths.random.SeedException;
import org.uncommons.maths.random.SeedGenerator;

public class CellularAutomatonRNG
extends Random
implements RepeatableRNG {
    private static final int SEED_SIZE_BYTES = 4;
    private static final int AUTOMATON_LENGTH = 2056;
    private static final int[] RNG_RULE = new int[]{100, 75, 16, 3, 229, 51, 197, 118, 24, 62, 198, 11, 141, 152, 241, 188, 2, 17, 71, 47, 179, 177, 126, 231, 202, 243, 59, 25, 77, 196, 30, 134, 199, 163, 34, 216, 21, 84, 37, 182, 224, 186, 64, 79, 225, 45, 143, 20, 48, 147, 209, 221, 125, 29, 99, 12, 46, 190, 102, 220, 80, 215, 242, 105, 15, 53, 0, 67, 68, 69, 70, 89, 109, 195, 170, 78, 210, 131, 42, 110, 181, 145, 40, 114, 254, 85, 107, 87, 72, 192, 90, 201, 162, 122, 86, 252, 94, 129, 98, 132, 193, 249, 156, 172, 219, 230, 153, 54, 180, 151, 83, 214, 123, 88, 164, 167, 116, 117, 7, 27, 23, 213, 235, 5, 65, 124, 60, 127, 236, 149, 44, 28, 58, 121, 191, 13, 250, 10, 232, 112, 101, 217, 183, 239, 8, 32, 228, 174, 49, 113, 247, 158, 106, 218, 154, 66, 226, 157, 50, 26, 253, 93, 205, 41, 133, 165, 61, 161, 187, 169, 6, 171, 81, 248, 56, 175, 246, 36, 178, 52, 57, 212, 39, 176, 184, 185, 245, 63, 35, 189, 206, 76, 104, 233, 194, 19, 43, 159, 108, 55, 200, 155, 14, 74, 244, 255, 222, 207, 208, 137, 128, 135, 96, 144, 18, 95, 234, 139, 173, 92, 1, 203, 115, 223, 130, 97, 91, 227, 146, 4, 31, 120, 211, 38, 22, 138, 140, 237, 238, 251, 240, 160, 142, 119, 73, 103, 166, 33, 148, 9, 111, 136, 168, 150, 82, 204, 100, 75, 16, 3, 229, 51, 197, 118, 24, 62, 198, 11, 141, 152, 241, 188, 2, 17, 71, 47, 179, 177, 126, 231, 202, 243, 59, 25, 77, 196, 30, 134, 199, 163, 34, 216, 21, 84, 37, 182, 224, 186, 64, 79, 225, 45, 143, 20, 48, 147, 209, 221, 125, 29, 99, 12, 46, 190, 102, 220, 80, 215, 242, 105, 15, 53, 0, 67, 68, 69, 70, 89, 109, 195, 170, 78, 210, 131, 42, 110, 181, 145, 40, 114, 254, 85, 107, 87, 72, 192, 90, 201, 162, 122, 86, 252, 94, 129, 98, 132, 193, 249, 156, 172, 219, 230, 153, 54, 180, 151, 83, 214, 123, 88, 164, 167, 116, 117, 7, 27, 23, 213, 235, 5, 65, 124, 60, 127, 236, 149, 44, 28, 58, 121, 191, 13, 250, 10, 232, 112, 101, 217, 183, 239, 8, 32, 228, 174, 49, 113, 247, 158, 106, 218, 154, 66, 226, 157, 50, 26, 253, 93, 205, 41, 133, 165, 61, 161, 187, 169, 6, 171, 81, 248, 56, 175, 246, 36, 178, 52, 57, 212, 39, 176, 184, 185, 245, 63, 35, 189, 206, 76, 104, 233, 194, 19, 43, 159, 108, 55, 200, 155, 14, 74, 244, 255, 222, 207, 208, 137, 128, 135, 96, 144, 18, 95, 234, 139, 173, 92, 1, 203, 115, 223, 130, 97, 91, 227, 146, 4, 31, 120, 211, 38, 22, 138, 140, 237, 238, 251, 240, 160, 142, 119, 73, 103, 166, 33, 148, 9, 111, 136, 168, 150, 82};
    private final byte[] seed;
    private final int[] cells = new int[2056];
    private final ReentrantLock lock = new ReentrantLock();
    private int currentCellIndex = 2055;

    public CellularAutomatonRNG() {
        this(DefaultSeedGenerator.getInstance().generateSeed(4));
    }

    public CellularAutomatonRNG(SeedGenerator seedGenerator) throws SeedException {
        this(seedGenerator.generateSeed(4));
    }

    public CellularAutomatonRNG(byte[] seed) {
        int i;
        if (seed == null || seed.length != 4) {
            throw new IllegalArgumentException("Cellular Automaton RNG requires a 32-bit (4-byte) seed.");
        }
        this.seed = (byte[])seed.clone();
        this.cells[2055] = seed[0] + 128;
        this.cells[2054] = seed[1] + 128;
        this.cells[2053] = seed[2] + 128;
        this.cells[2052] = seed[3] + 128;
        int seedAsInt = BinaryUtils.convertBytesToInt(seed, 0);
        if (seedAsInt != -1) {
            ++seedAsInt;
        }
        for (i = 0; i < 2052; ++i) {
            this.cells[i] = 0xFF & seedAsInt >> i % 32;
        }
        for (i = 0; i < 0x102010; ++i) {
            this.next(32);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public int next(int bits) {
        int result;
        try {
            this.lock.lock();
            int cellC = this.currentCellIndex - 1;
            int cellB = cellC - 1;
            int cellA = cellB - 1;
            this.cells[this.currentCellIndex] = RNG_RULE[this.cells[cellC] + this.cells[this.currentCellIndex]];
            this.cells[cellC] = RNG_RULE[this.cells[cellB] + this.cells[cellC]];
            this.cells[cellB] = RNG_RULE[this.cells[cellA] + this.cells[cellB]];
            if (cellA == 0) {
                this.cells[cellA] = RNG_RULE[this.cells[cellA]];
                this.currentCellIndex = 2055;
            } else {
                this.cells[cellA] = RNG_RULE[this.cells[cellA - 1] + this.cells[cellA]];
                this.currentCellIndex -= 4;
            }
            result = CellularAutomatonRNG.convertCellsToInt(this.cells, cellA);
        }
        finally {
            this.lock.unlock();
        }
        return result >>> 32 - bits;
    }

    public byte[] getSeed() {
        return this.seed;
    }

    private static int convertCellsToInt(int[] cells, int offset) {
        return cells[offset] + (cells[offset + 1] << 8) + (cells[offset + 2] << 16) + (cells[offset + 3] << 24);
    }
}

