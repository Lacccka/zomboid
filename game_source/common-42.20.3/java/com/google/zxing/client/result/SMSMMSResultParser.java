/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.client.result;

import com.google.zxing.Result;
import com.google.zxing.client.result.ResultParser;
import com.google.zxing.client.result.SMSParsedResult;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public final class SMSMMSResultParser
extends ResultParser {
    @Override
    public SMSParsedResult parse(Result result) {
        int comma;
        int queryStart;
        String rawText = SMSMMSResultParser.getMassagedText(result);
        if (!(rawText.startsWith("sms:") || rawText.startsWith("SMS:") || rawText.startsWith("mms:") || rawText.startsWith("MMS:"))) {
            return null;
        }
        Map<String, String> nameValuePairs = SMSMMSResultParser.parseNameValuePairs(rawText);
        String subject = null;
        String body = null;
        boolean querySyntax = false;
        if (nameValuePairs != null && !nameValuePairs.isEmpty()) {
            subject = nameValuePairs.get("subject");
            body = nameValuePairs.get("body");
            querySyntax = true;
        }
        String smsURIWithoutQuery = (queryStart = rawText.indexOf(63, 4)) < 0 || !querySyntax ? rawText.substring(4) : rawText.substring(4, queryStart);
        int lastComma = -1;
        ArrayList<String> numbers = new ArrayList<String>(1);
        ArrayList<String> vias = new ArrayList<String>(1);
        while ((comma = smsURIWithoutQuery.indexOf(44, lastComma + 1)) > lastComma) {
            String numberPart = smsURIWithoutQuery.substring(lastComma + 1, comma);
            SMSMMSResultParser.addNumberVia(numbers, vias, numberPart);
            lastComma = comma;
        }
        SMSMMSResultParser.addNumberVia(numbers, vias, smsURIWithoutQuery.substring(lastComma + 1));
        return new SMSParsedResult(numbers.toArray(new String[numbers.size()]), vias.toArray(new String[vias.size()]), subject, body);
    }

    private static void addNumberVia(Collection<String> numbers, Collection<String> vias, String numberPart) {
        int numberEnd = numberPart.indexOf(59);
        if (numberEnd < 0) {
            numbers.add(numberPart);
            vias.add(null);
        } else {
            numbers.add(numberPart.substring(0, numberEnd));
            String maybeVia = numberPart.substring(numberEnd + 1);
            String via = maybeVia.startsWith("via=") ? maybeVia.substring(4) : null;
            vias.add(via);
        }
    }
}

