/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing;

import com.google.zxing.LuminanceSource;

public final class PlanarYUVLuminanceSource
extends LuminanceSource {
    private static final int THUMBNAIL_SCALE_FACTOR = 2;
    private final byte[] yuvData;
    private final int dataWidth;
    private final int dataHeight;
    private final int left;
    private final int top;

    public PlanarYUVLuminanceSource(byte[] yuvData, int dataWidth, int dataHeight, int left, int top, int width, int height, boolean reverseHorizontal) {
        super(width, height);
        if (left + width > dataWidth || top + height > dataHeight) {
            throw new IllegalArgumentException("Crop rectangle does not fit within image data.");
        }
        this.yuvData = yuvData;
        this.dataWidth = dataWidth;
        this.dataHeight = dataHeight;
        this.left = left;
        this.top = top;
        if (reverseHorizontal) {
            this.reverseHorizontal(width, height);
        }
    }

    @Override
    public byte[] getRow(int y, byte[] row) {
        if (y < 0 || y >= this.getHeight()) {
            throw new IllegalArgumentException("Requested row is outside the image: " + y);
        }
        int width = this.getWidth();
        if (row == null || row.length < width) {
            row = new byte[width];
        }
        int offset = (y + this.top) * this.dataWidth + this.left;
        System.arraycopy(this.yuvData, offset, row, 0, width);
        return row;
    }

    @Override
    public byte[] getMatrix() {
        int width = this.getWidth();
        int height = this.getHeight();
        if (width == this.dataWidth && height == this.dataHeight) {
            return this.yuvData;
        }
        int area = width * height;
        byte[] matrix = new byte[area];
        int inputOffset = this.top * this.dataWidth + this.left;
        if (width == this.dataWidth) {
            System.arraycopy(this.yuvData, inputOffset, matrix, 0, area);
            return matrix;
        }
        byte[] yuv = this.yuvData;
        for (int y = 0; y < height; ++y) {
            int outputOffset = y * width;
            System.arraycopy(yuv, inputOffset, matrix, outputOffset, width);
            inputOffset += this.dataWidth;
        }
        return matrix;
    }

    @Override
    public boolean isCropSupported() {
        return true;
    }

    @Override
    public LuminanceSource crop(int left, int top, int width, int height) {
        return new PlanarYUVLuminanceSource(this.yuvData, this.dataWidth, this.dataHeight, this.left + left, this.top + top, width, height, false);
    }

    public int[] renderThumbnail() {
        int width = this.getWidth() / 2;
        int height = this.getHeight() / 2;
        int[] pixels = new int[width * height];
        byte[] yuv = this.yuvData;
        int inputOffset = this.top * this.dataWidth + this.left;
        for (int y = 0; y < height; ++y) {
            int outputOffset = y * width;
            for (int x = 0; x < width; ++x) {
                int grey = yuv[inputOffset + x * 2] & 0xFF;
                pixels[outputOffset + x] = 0xFF000000 | grey * 65793;
            }
            inputOffset += this.dataWidth * 2;
        }
        return pixels;
    }

    public int getThumbnailWidth() {
        return this.getWidth() / 2;
    }

    public int getThumbnailHeight() {
        return this.getHeight() / 2;
    }

    private void reverseHorizontal(int width, int height) {
        byte[] yuvData = this.yuvData;
        int y = 0;
        int rowStart = this.top * this.dataWidth + this.left;
        while (y < height) {
            int middle = rowStart + width / 2;
            int x1 = rowStart;
            int x2 = rowStart + width - 1;
            while (x1 < middle) {
                byte temp = yuvData[x1];
                yuvData[x1] = yuvData[x2];
                yuvData[x2] = temp;
                ++x1;
                --x2;
            }
            ++y;
            rowStart += this.dataWidth;
        }
    }
}

