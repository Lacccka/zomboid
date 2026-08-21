/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.client.result;

import com.google.zxing.Result;
import com.google.zxing.client.result.EmailAddressParsedResult;
import com.google.zxing.client.result.ResultParser;

public final class SMTPResultParser
extends ResultParser {
    @Override
    public EmailAddressParsedResult parse(Result result) {
        String rawText = SMTPResultParser.getMassagedText(result);
        if (!rawText.startsWith("smtp:") && !rawText.startsWith("SMTP:")) {
            return null;
        }
        String emailAddress = rawText.substring(5);
        String subject = null;
        String body = null;
        int colon = emailAddress.indexOf(58);
        if (colon >= 0) {
            subject = emailAddress.substring(colon + 1);
            emailAddress = emailAddress.substring(0, colon);
            colon = subject.indexOf(58);
            if (colon >= 0) {
                body = subject.substring(colon + 1);
                subject = subject.substring(0, colon);
            }
        }
        return new EmailAddressParsedResult(new String[]{emailAddress}, null, null, subject, body);
    }
}

