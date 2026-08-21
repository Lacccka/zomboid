/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.multi;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import java.util.Map;

public final class ByQuadrantReader
implements Reader {
    private final Reader delegate;

    public ByQuadrantReader(Reader delegate) {
        this.delegate = delegate;
    }

    @Override
    public Result decode(BinaryBitmap image) throws NotFoundException, ChecksumException, FormatException {
        return this.decode(image, null);
    }

    @Override
    public Result decode(BinaryBitmap image, Map<DecodeHintType, ?> hints) throws NotFoundException, ChecksumException, FormatException {
        int width = image.getWidth();
        int height = image.getHeight();
        int halfWidth = width / 2;
        int halfHeight = height / 2;
        try {
            return this.delegate.decode(image.crop(0, 0, halfWidth, halfHeight), hints);
        }
        catch (NotFoundException notFoundException) {
            try {
                Result result = this.delegate.decode(image.crop(halfWidth, 0, halfWidth, halfHeight), hints);
                ByQuadrantReader.makeAbsolute(result.getResultPoints(), halfWidth, 0);
                return result;
            }
            catch (NotFoundException result) {
                try {
                    Result result2 = this.delegate.decode(image.crop(0, halfHeight, halfWidth, halfHeight), hints);
                    ByQuadrantReader.makeAbsolute(result2.getResultPoints(), 0, halfHeight);
                    return result2;
                }
                catch (NotFoundException result2) {
                    try {
                        Result result3 = this.delegate.decode(image.crop(halfWidth, halfHeight, halfWidth, halfHeight), hints);
                        ByQuadrantReader.makeAbsolute(result3.getResultPoints(), halfWidth, halfHeight);
                        return result3;
                    }
                    catch (NotFoundException result3) {
                        int quarterWidth = halfWidth / 2;
                        int quarterHeight = halfHeight / 2;
                        BinaryBitmap center = image.crop(quarterWidth, quarterHeight, halfWidth, halfHeight);
                        Result result4 = this.delegate.decode(center, hints);
                        ByQuadrantReader.makeAbsolute(result4.getResultPoints(), quarterWidth, quarterHeight);
                        return result4;
                    }
                }
            }
        }
    }

    @Override
    public void reset() {
        this.delegate.reset();
    }

    private static void makeAbsolute(ResultPoint[] points, int leftOffset, int topOffset) {
        if (points != null) {
            for (int i = 0; i < points.length; ++i) {
                ResultPoint relative = points[i];
                points[i] = new ResultPoint(relative.getX() + (float)leftOffset, relative.getY() + (float)topOffset);
            }
        }
    }
}

