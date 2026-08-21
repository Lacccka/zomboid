/*
 * Decompiled with CFR 0.152.
 */
package de.taimos.totp;

import java.util.Random;
import org.apache.commons.codec.binary.Base32;

public final class TOTPData {
    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();
    private static final Random rnd = new Random();
    private final String issuer;
    private final String user;
    private final byte[] secret;

    public TOTPData(String issuer, String user, byte[] secret) {
        this.issuer = issuer;
        this.user = user;
        this.secret = secret;
    }

    public TOTPData(byte[] secret) {
        this(null, null, secret);
    }

    public String getIssuer() {
        return this.issuer;
    }

    public String getUser() {
        return this.user;
    }

    public byte[] getSecret() {
        return this.secret;
    }

    public String getSecretAsHex() {
        char[] hexChars = new char[this.secret.length * 2];
        for (int j = 0; j < this.secret.length; ++j) {
            int v = this.secret[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0xF];
        }
        return new String(hexChars);
    }

    public String getSecretAsBase32() {
        Base32 base = new Base32();
        return base.encodeToString(this.secret);
    }

    public String getUrl() {
        String secretString = this.getSecretAsBase32();
        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s", this.issuer, this.user, secretString, this.issuer);
    }

    public String getSerial() {
        return String.format("otpauth://totp/%s:%s", this.issuer, this.user);
    }

    public static TOTPData create() {
        return new TOTPData(TOTPData.createSecret());
    }

    public static TOTPData create(String issuer, String user) {
        return new TOTPData(issuer, user, TOTPData.createSecret());
    }

    public static byte[] createSecret() {
        byte[] secret = new byte[20];
        rnd.nextBytes(secret);
        return secret;
    }
}

