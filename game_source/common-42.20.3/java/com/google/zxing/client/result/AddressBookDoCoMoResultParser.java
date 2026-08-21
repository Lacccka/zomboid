/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.client.result;

import com.google.zxing.Result;
import com.google.zxing.client.result.AbstractDoCoMoResultParser;
import com.google.zxing.client.result.AddressBookParsedResult;

public final class AddressBookDoCoMoResultParser
extends AbstractDoCoMoResultParser {
    @Override
    public AddressBookParsedResult parse(Result result) {
        String rawText = AddressBookDoCoMoResultParser.getMassagedText(result);
        if (!rawText.startsWith("MECARD:")) {
            return null;
        }
        String[] rawName = AddressBookDoCoMoResultParser.matchDoCoMoPrefixedField("N:", rawText, true);
        if (rawName == null) {
            return null;
        }
        String name = AddressBookDoCoMoResultParser.parseName(rawName[0]);
        String pronunciation = AddressBookDoCoMoResultParser.matchSingleDoCoMoPrefixedField("SOUND:", rawText, true);
        String[] phoneNumbers = AddressBookDoCoMoResultParser.matchDoCoMoPrefixedField("TEL:", rawText, true);
        String[] emails = AddressBookDoCoMoResultParser.matchDoCoMoPrefixedField("EMAIL:", rawText, true);
        String note = AddressBookDoCoMoResultParser.matchSingleDoCoMoPrefixedField("NOTE:", rawText, false);
        String[] addresses = AddressBookDoCoMoResultParser.matchDoCoMoPrefixedField("ADR:", rawText, true);
        String birthday = AddressBookDoCoMoResultParser.matchSingleDoCoMoPrefixedField("BDAY:", rawText, true);
        if (!AddressBookDoCoMoResultParser.isStringOfDigits(birthday, 8)) {
            birthday = null;
        }
        String[] urls2 = AddressBookDoCoMoResultParser.matchDoCoMoPrefixedField("URL:", rawText, true);
        String org = AddressBookDoCoMoResultParser.matchSingleDoCoMoPrefixedField("ORG:", rawText, true);
        return new AddressBookParsedResult(AddressBookDoCoMoResultParser.maybeWrap(name), null, pronunciation, phoneNumbers, null, emails, null, null, note, addresses, null, org, birthday, null, urls2, null);
    }

    private static String parseName(String name) {
        int comma = name.indexOf(44);
        if (comma >= 0) {
            return name.substring(comma + 1) + ' ' + name.substring(0, comma);
        }
        return name;
    }
}

