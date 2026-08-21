/*
 * Decompiled with CFR 0.152.
 */
package org.lwjglx.opengl;

import org.lwjglx.opengl.PixelFormatLWJGL;

public final class PixelFormat
implements PixelFormatLWJGL {
    private int bpp;
    private int alpha;
    private int depth;
    private int stencil;
    private int samples;
    private int colorSamples;
    private int numAuxBuffers;
    private int accumBpp;
    private int accumAlpha;
    private boolean stereo;
    private boolean floatingPoint;
    private boolean floatingPointPacked;
    private boolean sRgb;

    public PixelFormat() {
        this(0, 8, 0);
    }

    public PixelFormat(int alpha, int depth, int stencil) {
        this(alpha, depth, stencil, 0);
    }

    public PixelFormat(int alpha, int depth, int stencil, int samples) {
        this(0, alpha, depth, stencil, samples);
    }

    public PixelFormat(int bpp, int alpha, int depth, int stencil, int samples) {
        this(bpp, alpha, depth, stencil, samples, 0, 0, 0, false);
    }

    public PixelFormat(int bpp, int alpha, int depth, int stencil, int samples, int numAuxBuffers, int accumBpp, int accumAlpha, boolean stereo) {
        this(bpp, alpha, depth, stencil, samples, numAuxBuffers, accumBpp, accumAlpha, stereo, false);
    }

    public PixelFormat(int bpp, int alpha, int depth, int stencil, int samples, int numAuxBuffers, int accumBpp, int accumAlpha, boolean stereo, boolean floatingPoint) {
        this.bpp = bpp;
        this.alpha = alpha;
        this.depth = depth;
        this.stencil = stencil;
        this.samples = samples;
        this.numAuxBuffers = numAuxBuffers;
        this.accumBpp = accumBpp;
        this.accumAlpha = accumAlpha;
        this.stereo = stereo;
        this.floatingPoint = floatingPoint;
        this.floatingPointPacked = false;
        this.sRgb = false;
    }

    private PixelFormat(PixelFormat pf) {
        this.bpp = pf.bpp;
        this.alpha = pf.alpha;
        this.depth = pf.depth;
        this.stencil = pf.stencil;
        this.samples = pf.samples;
        this.colorSamples = pf.colorSamples;
        this.numAuxBuffers = pf.numAuxBuffers;
        this.accumBpp = pf.accumBpp;
        this.accumAlpha = pf.accumAlpha;
        this.stereo = pf.stereo;
        this.floatingPoint = pf.floatingPoint;
        this.floatingPointPacked = pf.floatingPointPacked;
        this.sRgb = pf.sRgb;
    }

    public int getBitsPerPixel() {
        return this.bpp;
    }

    public PixelFormat withBitsPerPixel(int bpp) {
        if (bpp < 0) {
            throw new IllegalArgumentException("Invalid number of bits per pixel specified: " + bpp);
        }
        PixelFormat pf = new PixelFormat(this);
        pf.bpp = bpp;
        return pf;
    }

    public int getAlphaBits() {
        return this.alpha;
    }

    public PixelFormat withAlphaBits(int alpha) {
        if (alpha < 0) {
            throw new IllegalArgumentException("Invalid number of alpha bits specified: " + alpha);
        }
        PixelFormat pf = new PixelFormat(this);
        pf.alpha = alpha;
        return pf;
    }

    public int getDepthBits() {
        return this.depth;
    }

    public PixelFormat withDepthBits(int depth) {
        if (depth < 0) {
            throw new IllegalArgumentException("Invalid number of depth bits specified: " + depth);
        }
        PixelFormat pf = new PixelFormat(this);
        pf.depth = depth;
        return pf;
    }

    public int getStencilBits() {
        return this.stencil;
    }

    public PixelFormat withStencilBits(int stencil) {
        if (stencil < 0) {
            throw new IllegalArgumentException("Invalid number of stencil bits specified: " + stencil);
        }
        PixelFormat pf = new PixelFormat(this);
        pf.stencil = stencil;
        return pf;
    }

    public int getSamples() {
        return this.samples;
    }

    public PixelFormat withSamples(int samples) {
        if (samples < 0) {
            throw new IllegalArgumentException("Invalid number of samples specified: " + samples);
        }
        PixelFormat pf = new PixelFormat(this);
        pf.samples = samples;
        return pf;
    }

    public PixelFormat withCoverageSamples(int colorSamples) {
        return this.withCoverageSamples(colorSamples, this.samples);
    }

    public PixelFormat withCoverageSamples(int colorSamples, int coverageSamples) {
        if (coverageSamples < 0 || colorSamples < 0 || coverageSamples == 0 && 0 < colorSamples || coverageSamples < colorSamples) {
            throw new IllegalArgumentException("Invalid number of coverage samples specified: " + coverageSamples + " - " + colorSamples);
        }
        PixelFormat pf = new PixelFormat(this);
        pf.samples = coverageSamples;
        pf.colorSamples = colorSamples;
        return pf;
    }

    public int getAuxBuffers() {
        return this.numAuxBuffers;
    }

    public PixelFormat withAuxBuffers(int num_aux_buffers) {
        if (num_aux_buffers < 0) {
            throw new IllegalArgumentException("Invalid number of auxiliary buffers specified: " + num_aux_buffers);
        }
        PixelFormat pf = new PixelFormat(this);
        pf.numAuxBuffers = num_aux_buffers;
        return pf;
    }

    public int getAccumulationBitsPerPixel() {
        return this.accumBpp;
    }

    public PixelFormat withAccumulationBitsPerPixel(int accum_bpp) {
        if (accum_bpp < 0) {
            throw new IllegalArgumentException("Invalid number of bits per pixel in the accumulation buffer specified: " + accum_bpp);
        }
        PixelFormat pf = new PixelFormat(this);
        pf.accumBpp = accum_bpp;
        return pf;
    }

    public int getAccumulationAlpha() {
        return this.accumAlpha;
    }

    public PixelFormat withAccumulationAlpha(int accum_alpha) {
        if (accum_alpha < 0) {
            throw new IllegalArgumentException("Invalid number of alpha bits in the accumulation buffer specified: " + accum_alpha);
        }
        PixelFormat pf = new PixelFormat(this);
        pf.accumAlpha = accum_alpha;
        return pf;
    }

    public boolean isStereo() {
        return this.stereo;
    }

    public PixelFormat withStereo(boolean stereo) {
        PixelFormat pf = new PixelFormat(this);
        pf.stereo = stereo;
        return pf;
    }

    public boolean isFloatingPoint() {
        return this.floatingPoint;
    }

    public PixelFormat withFloatingPoint(boolean floating_point) {
        PixelFormat pf = new PixelFormat(this);
        pf.floatingPoint = floating_point;
        if (floating_point) {
            pf.floatingPointPacked = false;
        }
        return pf;
    }

    public PixelFormat withFloatingPointPacked(boolean floating_point_packed) {
        PixelFormat pf = new PixelFormat(this);
        pf.floatingPointPacked = floating_point_packed;
        if (floating_point_packed) {
            pf.floatingPoint = false;
        }
        return pf;
    }

    public boolean isSRGB() {
        return this.sRgb;
    }

    public PixelFormat withSRGB(boolean sRGB) {
        PixelFormat pf = new PixelFormat(this);
        pf.sRgb = sRGB;
        return pf;
    }
}

