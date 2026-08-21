/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.multi.qrcode.detector;

import com.google.zxing.DecodeHintType;
import com.google.zxing.NotFoundException;
import com.google.zxing.ReaderException;
import com.google.zxing.ResultPointCallback;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.DetectorResult;
import com.google.zxing.multi.qrcode.detector.MultiFinderPatternFinder;
import com.google.zxing.qrcode.detector.Detector;
import com.google.zxing.qrcode.detector.FinderPatternInfo;
import java.util.ArrayList;
import java.util.Map;

public final class MultiDetector
extends Detector {
    private static final DetectorResult[] EMPTY_DETECTOR_RESULTS = new DetectorResult[0];

    public MultiDetector(BitMatrix image) {
        super(image);
    }

    public DetectorResult[] detectMulti(Map<DecodeHintType, ?> hints) throws NotFoundException {
        ResultPointCallback resultPointCallback;
        BitMatrix image = this.getImage();
        MultiFinderPatternFinder finder = new MultiFinderPatternFinder(image, resultPointCallback = hints == null ? null : (ResultPointCallback)hints.get((Object)DecodeHintType.NEED_RESULT_POINT_CALLBACK));
        FinderPatternInfo[] infos = finder.findMulti(hints);
        if (infos.length == 0) {
            throw NotFoundException.getNotFoundInstance();
        }
        ArrayList<DetectorResult> result = new ArrayList<DetectorResult>();
        for (FinderPatternInfo info : infos) {
            try {
                result.add(this.processFinderPatternInfo(info));
            }
            catch (ReaderException readerException) {
                // empty catch block
            }
        }
        if (result.isEmpty()) {
            return EMPTY_DETECTOR_RESULTS;
        }
        return result.toArray(new DetectorResult[result.size()]);
    }
}

