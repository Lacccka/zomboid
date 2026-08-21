/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.NotFoundException;
import com.google.zxing.Reader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.aztec.AztecReader;
import com.google.zxing.datamatrix.DataMatrixReader;
import com.google.zxing.maxicode.MaxiCodeReader;
import com.google.zxing.oned.MultiFormatOneDReader;
import com.google.zxing.pdf417.PDF417Reader;
import com.google.zxing.qrcode.QRCodeReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public final class MultiFormatReader
implements Reader {
    private Map<DecodeHintType, ?> hints;
    private Reader[] readers;

    @Override
    public Result decode(BinaryBitmap image) throws NotFoundException {
        this.setHints(null);
        return this.decodeInternal(image);
    }

    @Override
    public Result decode(BinaryBitmap image, Map<DecodeHintType, ?> hints) throws NotFoundException {
        this.setHints(hints);
        return this.decodeInternal(image);
    }

    public Result decodeWithState(BinaryBitmap image) throws NotFoundException {
        if (this.readers == null) {
            this.setHints(null);
        }
        return this.decodeInternal(image);
    }

    public void setHints(Map<DecodeHintType, ?> hints) {
        this.hints = hints;
        boolean tryHarder = hints != null && hints.containsKey((Object)DecodeHintType.TRY_HARDER);
        Collection formats = hints == null ? null : (Collection)hints.get((Object)DecodeHintType.POSSIBLE_FORMATS);
        ArrayList<Reader> readers = new ArrayList<Reader>();
        if (formats != null) {
            boolean addOneDReader;
            boolean bl = addOneDReader = formats.contains((Object)BarcodeFormat.UPC_A) || formats.contains((Object)BarcodeFormat.UPC_E) || formats.contains((Object)BarcodeFormat.EAN_13) || formats.contains((Object)BarcodeFormat.EAN_8) || formats.contains((Object)BarcodeFormat.CODABAR) || formats.contains((Object)BarcodeFormat.CODE_39) || formats.contains((Object)BarcodeFormat.CODE_93) || formats.contains((Object)BarcodeFormat.CODE_128) || formats.contains((Object)BarcodeFormat.ITF) || formats.contains((Object)BarcodeFormat.RSS_14) || formats.contains((Object)BarcodeFormat.RSS_EXPANDED);
            if (addOneDReader && !tryHarder) {
                readers.add(new MultiFormatOneDReader(hints));
            }
            if (formats.contains((Object)BarcodeFormat.QR_CODE)) {
                readers.add(new QRCodeReader());
            }
            if (formats.contains((Object)BarcodeFormat.DATA_MATRIX)) {
                readers.add(new DataMatrixReader());
            }
            if (formats.contains((Object)BarcodeFormat.AZTEC)) {
                readers.add(new AztecReader());
            }
            if (formats.contains((Object)BarcodeFormat.PDF_417)) {
                readers.add(new PDF417Reader());
            }
            if (formats.contains((Object)BarcodeFormat.MAXICODE)) {
                readers.add(new MaxiCodeReader());
            }
            if (addOneDReader && tryHarder) {
                readers.add(new MultiFormatOneDReader(hints));
            }
        }
        if (readers.isEmpty()) {
            if (!tryHarder) {
                readers.add(new MultiFormatOneDReader(hints));
            }
            readers.add(new QRCodeReader());
            readers.add(new DataMatrixReader());
            readers.add(new AztecReader());
            readers.add(new PDF417Reader());
            readers.add(new MaxiCodeReader());
            if (tryHarder) {
                readers.add(new MultiFormatOneDReader(hints));
            }
        }
        this.readers = readers.toArray(new Reader[readers.size()]);
    }

    @Override
    public void reset() {
        if (this.readers != null) {
            for (Reader reader : this.readers) {
                reader.reset();
            }
        }
    }

    private Result decodeInternal(BinaryBitmap image) throws NotFoundException {
        if (this.readers != null) {
            for (Reader reader : this.readers) {
                try {
                    return reader.decode(image, this.hints);
                }
                catch (ReaderException readerException) {
                }
            }
        }
        throw NotFoundException.getNotFoundInstance();
    }
}

