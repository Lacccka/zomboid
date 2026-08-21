/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.client.result;

import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ParsedResultType;

public final class SMSParsedResult
extends ParsedResult {
    private final String[] numbers;
    private final String[] vias;
    private final String subject;
    private final String body;

    public SMSParsedResult(String number, String via, String subject, String body) {
        super(ParsedResultType.SMS);
        this.numbers = new String[]{number};
        this.vias = new String[]{via};
        this.subject = subject;
        this.body = body;
    }

    public SMSParsedResult(String[] numbers, String[] vias, String subject, String body) {
        super(ParsedResultType.SMS);
        this.numbers = numbers;
        this.vias = vias;
        this.subject = subject;
        this.body = body;
    }

    public String getSMSURI() {
        boolean hasSubject;
        StringBuilder result = new StringBuilder();
        result.append("sms:");
        boolean first = true;
        for (int i = 0; i < this.numbers.length; ++i) {
            if (first) {
                first = false;
            } else {
                result.append(',');
            }
            result.append(this.numbers[i]);
            if (this.vias == null || this.vias[i] == null) continue;
            result.append(";via=");
            result.append(this.vias[i]);
        }
        boolean hasBody = this.body != null;
        boolean bl = hasSubject = this.subject != null;
        if (hasBody || hasSubject) {
            result.append('?');
            if (hasBody) {
                result.append("body=");
                result.append(this.body);
            }
            if (hasSubject) {
                if (hasBody) {
                    result.append('&');
                }
                result.append("subject=");
                result.append(this.subject);
            }
        }
        return result.toString();
    }

    public String[] getNumbers() {
        return this.numbers;
    }

    public String[] getVias() {
        return this.vias;
    }

    public String getSubject() {
        return this.subject;
    }

    public String getBody() {
        return this.body;
    }

    @Override
    public String getDisplayResult() {
        StringBuilder result = new StringBuilder(100);
        SMSParsedResult.maybeAppend(this.numbers, result);
        SMSParsedResult.maybeAppend(this.subject, result);
        SMSParsedResult.maybeAppend(this.body, result);
        return result.toString();
    }
}

