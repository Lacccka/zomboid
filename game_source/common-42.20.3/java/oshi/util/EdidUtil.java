/*
 * Decompiled with CFR 0.152.
 */
package oshi.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.annotation.SuppressForbidden;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.util.ParseUtil;

@ThreadSafe
public final class EdidUtil {
    private static final Logger LOG = LoggerFactory.getLogger(EdidUtil.class);

    private EdidUtil() {
    }

    @SuppressForbidden(reason="customized base 2 parsing not in Util class")
    public static String getManufacturerID(byte[] edid) {
        String temp = String.format(Locale.ROOT, "%8s%8s", Integer.toBinaryString(edid[8] & 0xFF), Integer.toBinaryString(edid[9] & 0xFF)).replace(' ', '0');
        LOG.debug("Manufacurer ID: {}", (Object)temp);
        return String.format(Locale.ROOT, "%s%s%s", Character.valueOf((char)(64 + Integer.parseInt(temp.substring(1, 6), 2))), Character.valueOf((char)(64 + Integer.parseInt(temp.substring(6, 11), 2))), Character.valueOf((char)(64 + Integer.parseInt(temp.substring(11, 16), 2)))).replace("@", "");
    }

    public static String getProductID(byte[] edid) {
        return Integer.toHexString(ByteBuffer.wrap(Arrays.copyOfRange(edid, 10, 12)).order(ByteOrder.LITTLE_ENDIAN).getShort() & 0xFFFF);
    }

    public static String getSerialNo(byte[] edid) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Serial number: {}", (Object)Arrays.toString(Arrays.copyOfRange(edid, 12, 16)));
        }
        return String.format(Locale.ROOT, "%s%s%s%s", EdidUtil.getAlphaNumericOrHex(edid[15]), EdidUtil.getAlphaNumericOrHex(edid[14]), EdidUtil.getAlphaNumericOrHex(edid[13]), EdidUtil.getAlphaNumericOrHex(edid[12]));
    }

    private static String getAlphaNumericOrHex(byte b) {
        return Character.isLetterOrDigit((char)b) ? String.format(Locale.ROOT, "%s", Character.valueOf((char)b)) : String.format(Locale.ROOT, "%02X", b);
    }

    public static byte getWeek(byte[] edid) {
        return edid[16];
    }

    public static int getYear(byte[] edid) {
        byte temp = edid[17];
        LOG.debug("Year-1990: {}", (Object)temp);
        return temp + 1990;
    }

    public static String getVersion(byte[] edid) {
        return edid[18] + "." + edid[19];
    }

    public static boolean isDigital(byte[] edid) {
        return 1 == (edid[20] & 0xFF) >> 7;
    }

    public static int getHcm(byte[] edid) {
        return edid[21];
    }

    public static int getVcm(byte[] edid) {
        return edid[22];
    }

    public static byte[][] getDescriptors(byte[] edid) {
        byte[][] desc = new byte[4][18];
        for (int i = 0; i < desc.length; ++i) {
            System.arraycopy(edid, 54 + 18 * i, desc[i], 0, 18);
        }
        return desc;
    }

    public static int getDescriptorType(byte[] desc) {
        return ByteBuffer.wrap(Arrays.copyOfRange(desc, 0, 4)).getInt();
    }

    public static String getTimingDescriptor(byte[] desc) {
        int clock = ByteBuffer.wrap(Arrays.copyOfRange(desc, 0, 2)).order(ByteOrder.LITTLE_ENDIAN).getShort() / 100;
        int hActive = (desc[2] & 0xFF) + ((desc[4] & 0xF0) << 4);
        int vActive = (desc[5] & 0xFF) + ((desc[7] & 0xF0) << 4);
        return String.format(Locale.ROOT, "Clock %dMHz, Active Pixels %dx%d ", clock, hActive, vActive);
    }

    public static String getDescriptorRangeLimits(byte[] desc) {
        return String.format(Locale.ROOT, "Field Rate %d-%d Hz vertical, %d-%d Hz horizontal, Max clock: %d MHz", desc[5], desc[6], desc[7], desc[8], desc[9] * 10);
    }

    public static String getDescriptorText(byte[] desc) {
        return new String(Arrays.copyOfRange(desc, 4, 18), StandardCharsets.US_ASCII).trim();
    }

    public static String getPreferredResolution(byte[] edid) {
        int dtd = 54;
        int horizontalRes = (edid[dtd + 4] & 0xF0) << 4 | edid[dtd + 2] & 0xFF;
        int verticalRes = (edid[dtd + 7] & 0xF0) << 4 | edid[dtd + 5] & 0xFF;
        return horizontalRes + "x" + verticalRes;
    }

    public static String getModel(byte[] edid) {
        byte[][] desc = EdidUtil.getDescriptors(edid);
        String model = null;
        for (byte[] b : desc) {
            if (EdidUtil.getDescriptorType(b) != 252) continue;
            model = EdidUtil.getDescriptorText(b);
            break;
        }
        assert (model != null);
        String[] tokens = model.split("\\s+");
        if (tokens.length >= 1) {
            model = tokens[tokens.length - 1];
        }
        return model.trim();
    }

    public static String toString(byte[] edid) {
        byte[][] desc;
        StringBuilder sb = new StringBuilder();
        sb.append("  Manuf. ID=").append(EdidUtil.getManufacturerID(edid));
        sb.append(", Product ID=").append(EdidUtil.getProductID(edid));
        sb.append(", ").append(EdidUtil.isDigital(edid) ? "Digital" : "Analog");
        sb.append(", Serial=").append(EdidUtil.getSerialNo(edid));
        sb.append(", ManufDate=").append(EdidUtil.getWeek(edid) * 12 / 52 + 1).append('/').append(EdidUtil.getYear(edid));
        sb.append(", EDID v").append(EdidUtil.getVersion(edid));
        int hSize = EdidUtil.getHcm(edid);
        int vSize = EdidUtil.getVcm(edid);
        sb.append(String.format(Locale.ROOT, "%n  %d x %d cm (%.1f x %.1f in)", hSize, vSize, (double)hSize / 2.54, (double)vSize / 2.54));
        block8: for (byte[] b : desc = EdidUtil.getDescriptors(edid)) {
            switch (EdidUtil.getDescriptorType(b)) {
                case 255: {
                    sb.append("\n  Serial Number: ").append(EdidUtil.getDescriptorText(b));
                    continue block8;
                }
                case 254: {
                    sb.append("\n  Unspecified Text: ").append(EdidUtil.getDescriptorText(b));
                    continue block8;
                }
                case 253: {
                    sb.append("\n  Range Limits: ").append(EdidUtil.getDescriptorRangeLimits(b));
                    continue block8;
                }
                case 252: {
                    sb.append("\n  Monitor Name: ").append(EdidUtil.getDescriptorText(b));
                    continue block8;
                }
                case 251: {
                    sb.append("\n  White Point Data: ").append(ParseUtil.byteArrayToHexString(b));
                    continue block8;
                }
                case 250: {
                    sb.append("\n  Standard Timing ID: ").append(ParseUtil.byteArrayToHexString(b));
                    continue block8;
                }
                default: {
                    if (EdidUtil.getDescriptorType(b) <= 15 && EdidUtil.getDescriptorType(b) >= 0) {
                        sb.append("\n  Manufacturer Data: ").append(ParseUtil.byteArrayToHexString(b));
                        continue block8;
                    }
                    sb.append("\n  Preferred Timing: ").append(EdidUtil.getTimingDescriptor(b));
                }
            }
        }
        return sb.toString();
    }
}

