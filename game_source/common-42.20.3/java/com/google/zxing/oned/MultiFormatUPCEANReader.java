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
import com.google.zxing.oned.EAN13Reader;
import com.google.zxing.oned.EAN8Reader;
import com.google.zxing.oned.OneDReader;
import com.google.zxing.oned.UPCAReader;
import com.google.zxing.oned.UPCEANReader;
import com.google.zxing.oned.UPCEReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public final class MultiFormatUPCEANReader
extends OneDReader {
    private final UPCEANReader[] readers;

    public MultiFormatUPCEANReader(Map<DecodeHintType, ?> hints) {
        Collection possibleFormats = hints == null ? null : (Collection)hints.get((Object)DecodeHintType.POSSIBLE_FORMATS);
        ArrayList<UPCEANReader> readers = new ArrayList<UPCEANReader>();
        if (possibleFormats != null) {
            if (possibleFormats.contains((Object)BarcodeFormat.EAN_13)) {
                readers.add(new EAN13Reader());
            } else if (possibleFormats.contains((Object)BarcodeFormat.UPC_A)) {
                readers.add(new UPCAReader());
            }
            if (possibleFormats.contains((Object)BarcodeFormat.EAN_8)) {
                readers.add(new EAN8Reader());
            }
            if (possibleFormats.contains((Object)BarcodeFormat.UPC_E)) {
                readers.add(new UPCEReader());
            }
        }
        if (readers.isEmpty()) {
            readers.add(new EAN13Reader());
            readers.add(new EAN8Reader());
            readers.add(new UPCEReader());
        }
        this.readers = readers.toArray(new UPCEANReader[readers.size()]);
    }

    @Override
    public Result decodeRow(int rowNumber, BitArray row, Map<DecodeHintType, ?> hints) throws NotFoundException {
        int[] startGuardPattern = UPCEANReader.findStartGuardPattern(row);
        for (UPCEANReader reader : this.readers) {
            boolean canReturnUPCA;
            Result result;
            try {
                result = reader.decodeRow(rowNumber, row, startGuardPattern, hints);
            }
            catch (ReaderException ignored) {
                continue;
            }
            boolean ean13MayBeUPCA = result.getBarcodeFormat() == BarcodeFormat.EAN_13 && result.getText().charAt(0) == '0';
            Collection possibleFormats = hints == null ? null : (Collection)hints.get((Object)DecodeHintType.POSSIBLE_FORMATS);
            boolean bl = canReturnUPCA = possibleFormats == null || possibleFormats.contains((Object)BarcodeFormat.UPC_A);
            if (ean13MayBeUPCA && canReturnUPCA) {
                Result resultUPCA = new Result(result.getText().substring(1), result.getRawBytes(), result.getResultPoints(), BarcodeFormat.UPC_A);
                resultUPCA.putAllMetadata(result.getResultMetadata());
                return resultUPCA;
            }
            return result;
        }
        throw NotFoundException.getNotFoundInstance();
    }

    @Override
    public void reset() {
        for (UPCEANReader reader : this.readers) {
            reader.reset();
        }
    }
}

