/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.client.result;

import com.google.zxing.Result;
import com.google.zxing.client.result.AddressBookParsedResult;
import com.google.zxing.client.result.ResultParser;
import java.util.ArrayList;

public final class AddressBookAUResultParser
extends ResultParser {
    @Override
    public AddressBookParsedResult parse(Result result) {
        String[] stringArray;
        String rawText = AddressBookAUResultParser.getMassagedText(result);
        if (!rawText.contains("MEMORY") || !rawText.contains("\r\n")) {
            return null;
        }
        String name = AddressBookAUResultParser.matchSinglePrefixedField("NAME1:", rawText, '\r', true);
        String pronunciation = AddressBookAUResultParser.matchSinglePrefixedField("NAME2:", rawText, '\r', true);
        String[] phoneNumbers = AddressBookAUResultParser.matchMultipleValuePrefix("TEL", 3, rawText, true);
        String[] emails = AddressBookAUResultParser.matchMultipleValuePrefix("MAIL", 3, rawText, true);
        String note = AddressBookAUResultParser.matchSinglePrefixedField("MEMORY:", rawText, '\r', false);
        String address = AddressBookAUResultParser.matchSinglePrefixedField("ADD:", rawText, '\r', true);
        if (address == null) {
            stringArray = null;
        } else {
            String[] stringArray2 = new String[1];
            stringArray = stringArray2;
            stringArray2[0] = address;
        }
        String[] addresses = stringArray;
        return new AddressBookParsedResult(AddressBookAUResultParser.maybeWrap(name), null, pronunciation, phoneNumbers, null, emails, null, null, note, addresses, null, null, null, null, null, null);
    }

    private static String[] matchMultipleValuePrefix(String prefix, int max, String rawText, boolean trim) {
        String value;
        ArrayList<String> values2 = null;
        for (int i = 1; i <= max && (value = AddressBookAUResultParser.matchSinglePrefixedField(prefix + i + ':', rawText, '\r', trim)) != null; ++i) {
            if (values2 == null) {
                values2 = new ArrayList<String>(max);
            }
            values2.add(value);
        }
        if (values2 == null) {
            return null;
        }
        return values2.toArray(new String[values2.size()]);
    }
}

