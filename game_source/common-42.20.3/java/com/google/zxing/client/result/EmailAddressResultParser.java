/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.client.result;

import com.google.zxing.Result;
import com.google.zxing.client.result.EmailAddressParsedResult;
import com.google.zxing.client.result.EmailDoCoMoResultParser;
import com.google.zxing.client.result.ResultParser;
import java.util.Map;
import java.util.regex.Pattern;

public final class EmailAddressResultParser
extends ResultParser {
    private static final Pattern COMMA = Pattern.compile(",");

    @Override
    public EmailAddressParsedResult parse(Result result) {
        String rawText = EmailAddressResultParser.getMassagedText(result);
        if (rawText.startsWith("mailto:") || rawText.startsWith("MAILTO:")) {
            String hostEmail = rawText.substring(7);
            int queryStart = hostEmail.indexOf(63);
            if (queryStart >= 0) {
                hostEmail = hostEmail.substring(0, queryStart);
            }
            hostEmail = EmailAddressResultParser.urlDecode(hostEmail);
            String[] tos = null;
            if (!hostEmail.isEmpty()) {
                tos = COMMA.split(hostEmail);
            }
            Map<String, String> nameValues = EmailAddressResultParser.parseNameValuePairs(rawText);
            String[] ccs = null;
            String[] bccs = null;
            String subject = null;
            String body = null;
            if (nameValues != null) {
                String bccString;
                String ccString;
                String tosString;
                if (tos == null && (tosString = nameValues.get("to")) != null) {
                    tos = COMMA.split(tosString);
                }
                if ((ccString = nameValues.get("cc")) != null) {
                    ccs = COMMA.split(ccString);
                }
                if ((bccString = nameValues.get("bcc")) != null) {
                    bccs = COMMA.split(bccString);
                }
                subject = nameValues.get("subject");
                body = nameValues.get("body");
            }
            return new EmailAddressParsedResult(tos, ccs, bccs, subject, body);
        }
        if (!EmailDoCoMoResultParser.isBasicallyValidEmailAddress(rawText)) {
            return null;
        }
        return new EmailAddressParsedResult(rawText);
    }
}

