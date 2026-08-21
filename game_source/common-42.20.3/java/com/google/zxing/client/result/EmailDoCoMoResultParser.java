/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.client.result;

import com.google.zxing.Result;
import com.google.zxing.client.result.AbstractDoCoMoResultParser;
import com.google.zxing.client.result.EmailAddressParsedResult;
import java.util.regex.Pattern;

public final class EmailDoCoMoResultParser
extends AbstractDoCoMoResultParser {
    private static final Pattern ATEXT_ALPHANUMERIC = Pattern.compile("[a-zA-Z0-9@.!#$%&'*+\\-/=?^_`{|}~]+");

    @Override
    public EmailAddressParsedResult parse(Result result) {
        String rawText = EmailDoCoMoResultParser.getMassagedText(result);
        if (!rawText.startsWith("MATMSG:")) {
            return null;
        }
        String[] tos = EmailDoCoMoResultParser.matchDoCoMoPrefixedField("TO:", rawText, true);
        if (tos == null) {
            return null;
        }
        for (String to : tos) {
            if (EmailDoCoMoResultParser.isBasicallyValidEmailAddress(to)) continue;
            return null;
        }
        String subject = EmailDoCoMoResultParser.matchSingleDoCoMoPrefixedField("SUB:", rawText, false);
        String body = EmailDoCoMoResultParser.matchSingleDoCoMoPrefixedField("BODY:", rawText, false);
        return new EmailAddressParsedResult(tos, null, null, subject, body);
    }

    static boolean isBasicallyValidEmailAddress(String email) {
        return email != null && ATEXT_ALPHANUMERIC.matcher(email).matches() && email.indexOf(64) >= 0;
    }
}

