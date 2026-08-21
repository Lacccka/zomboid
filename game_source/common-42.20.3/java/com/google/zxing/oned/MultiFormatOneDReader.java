/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.oned;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.NotFoundException;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.BitArray;
import com.google.zxing.oned.CodaBarReader;
import com.google.zxing.oned.Code128Reader;
import com.google.zxing.oned.Code39Reader;
import com.google.zxing.oned.Code93Reader;
import com.google.zxing.oned.ITFReader;
import com.google.zxing.oned.MultiFormatUPCEANReader;
import com.google.zxing.oned.OneDReader;
import com.google.zxing.oned.rss.RSS14Reader;
import com.google.zxing.oned.rss.expanded.RSSExpandedReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public final class MultiFormatOneDReader
extends OneDReader {
    private final OneDReader[] readers;

    public MultiFormatOneDReader(Map<DecodeHintType, ?> hints) {
        Collection possibleFormats = hints == null ? null : (Collection)hints.get((Object)DecodeHintType.POSSIBLE_FORMATS);
        boolean useCode39CheckDigit = hints != null && hints.get((Object)DecodeHintType.ASSUME_CODE_39_CHECK_DIGIT) != null;
        ArrayList<OneDReader> readers = new ArrayList<OneDReader>();
        if (possibleFormats != null) {
            if (possibleFormats.contains((Object)BarcodeFormat.EAN_13) || possibleFormats.contains((Object)BarcodeFormat.UPC_A) || possibleFormats.contains((Object)BarcodeFormat.EAN_8) || possibleFormats.contains((Object)BarcodeFormat.UPC_E)) {
                readers.add(new MultiFormatUPCEANReader(hints));
            }
            if (possibleFormats.contains((Object)BarcodeFormat.CODE_39)) {
                readers.add(new Code39Reader(useCode39CheckDigit));
            }
            if (possibleFormats.contains((Object)BarcodeFormat.CODE_93)) {
                readers.add(new Code93Reader());
            }
            if (possibleFormats.contains((Object)BarcodeFormat.CODE_128)) {
                readers.add(new Code128Reader());
            }
            if (possibleFormats.contains((Object)BarcodeFormat.ITF)) {
                readers.add(new ITFReader());
            }
            if (possibleFormats.contains((Object)BarcodeFormat.CODABAR)) {
                readers.add(new CodaBarReader());
            }
            if (possibleFormats.contains((Object)BarcodeFormat.RSS_14)) {
                readers.add(new RSS14Reader());
            }
            if (possibleFormats.contains((Object)BarcodeFormat.RSS_EXPANDED)) {
                readers.add(new RSSExpandedReader());
            }
        }
        if (readers.isEmpty()) {
            readers.add(new MultiFormatUPCEANReader(hints));
            readers.add(new Code39Reader());
            readers.add(new CodaBarReader());
            readers.add(new Code93Reader());
            readers.add(new Code128Reader());
            readers.add(new ITFReader());
            readers.add(new RSS14Reader());
            readers.add(new RSSExpandedReader());
        }
        this.readers = readers.toArray(new OneDReader[readers.size()]);
    }

    @Override
    public Result decodeRow(int rowNumber, BitArray row, Map<DecodeHintType, ?> hints) throws NotFoundException {
        for (OneDReader reader : this.readers) {
            try {
                return reader.decodeRow(rowNumber, row, hints);
            }
            catch (ReaderException readerException) {
            }
        }
        throw NotFoundException.getNotFoundInstance();
    }

    @Override
    public void reset() {
        for (OneDReader reader : this.readers) {
            reader.reset();
        }
    }
}

