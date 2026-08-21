/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.client.result;

import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ParsedResultType;

public final class EmailAddressParsedResult
extends ParsedResult {
    private final String[] tos;
    private final String[] ccs;
    private final String[] bccs;
    private final String subject;
    private final String body;

    EmailAddressParsedResult(String to) {
        this(new String[]{to}, null, null, null, null);
    }

    EmailAddressParsedResult(String[] tos, String[] ccs, String[] bccs, String subject, String body) {
        super(ParsedResultType.EMAIL_ADDRESS);
        this.tos = tos;
        this.ccs = ccs;
        this.bccs = bccs;
        this.subject = subject;
        this.body = body;
    }

    @Deprecated
    public String getEmailAddress() {
        return this.tos == null || this.tos.length == 0 ? null : this.tos[0];
    }

    public String[] getTos() {
        return this.tos;
    }

    public String[] getCCs() {
        return this.ccs;
    }

    public String[] getBCCs() {
        return this.bccs;
    }

    public String getSubject() {
        return this.subject;
    }

    public String getBody() {
        return this.body;
    }

    @Deprecated
    public String getMailtoURI() {
        return "mailto:";
    }

    @Override
    public String getDisplayResult() {
        StringBuilder result = new StringBuilder(30);
        EmailAddressParsedResult.maybeAppend(this.tos, result);
        EmailAddressParsedResult.maybeAppend(this.ccs, result);
        EmailAddressParsedResult.maybeAppend(this.bccs, result);
        EmailAddressParsedResult.maybeAppend(this.subject, result);
        EmailAddressParsedResult.maybeAppend(this.body, result);
        return result.toString();
    }
}

