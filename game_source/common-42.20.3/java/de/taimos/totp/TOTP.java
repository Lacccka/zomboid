/*
 * Decompiled with CFR 0.152.
 */
package de.taimos.totp;

import java.lang.reflect.UndeclaredThrowableException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class TOTP {
    private TOTP() {
    }

    public static String getOTP(String key) {
        return TOTP.getOTP(TOTP.getStep(), key);
    }

    public static boolean validate(String key, String otp) {
        return TOTP.validate(TOTP.getStep(), key, otp);
    }

    private static boolean validate(long step, String key, String otp) {
        return TOTP.getOTP(step, key).equals(otp) || TOTP.getOTP(step - 1L, key).equals(otp);
    }

    private static long getStep() {
        return System.currentTimeMillis() / 30000L;
    }

    private static String getOTP(long step, String key) {
        String steps = Long.toHexString(step).toUpperCase();
        while (steps.length() < 16) {
            steps = "0" + steps;
        }
        byte[] msg = TOTP.hexStr2Bytes(steps);
        byte[] k = TOTP.hexStr2Bytes(key);
        byte[] hash = TOTP.hmac_sha1(k, msg);
        int offset = hash[hash.length - 1] & 0xF;
        int binary = (hash[offset] & 0x7F) << 24 | (hash[offset + 1] & 0xFF) << 16 | (hash[offset + 2] & 0xFF) << 8 | hash[offset + 3] & 0xFF;
        int otp = binary % 1000000;
        String result = Integer.toString(otp);
        while (result.length() < 6) {
            result = "0" + result;
        }
        return result;
    }

    private static byte[] hexStr2Bytes(String hex) {
        byte[] bArray = new BigInteger("10" + hex, 16).toByteArray();
        byte[] ret = new byte[bArray.length - 1];
        System.arraycopy(bArray, 1, ret, 0, ret.length);
        return ret;
    }

    private static byte[] hmac_sha1(byte[] keyBytes, byte[] text) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA1");
            SecretKeySpec macKey = new SecretKeySpec(keyBytes, "RAW");
            hmac.init(macKey);
            return hmac.doFinal(text);
        }
        catch (GeneralSecurityException gse) {
            throw new UndeclaredThrowableException(gse);
        }
    }
}

