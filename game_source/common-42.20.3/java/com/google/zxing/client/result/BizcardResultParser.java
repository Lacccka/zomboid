/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.client.result;

import com.google.zxing.Result;
import com.google.zxing.client.result.AbstractDoCoMoResultParser;
import com.google.zxing.client.result.AddressBookParsedResult;
import java.util.ArrayList;

public final class BizcardResultParser
extends AbstractDoCoMoResultParser {
    @Override
    public AddressBookParsedResult parse(Result result) {
        String rawText = BizcardResultParser.getMassagedText(result);
        if (!rawText.startsWith("BIZCARD:")) {
            return null;
        }
        String firstName = BizcardResultParser.matchSingleDoCoMoPrefixedField("N:", rawText, true);
        String lastName = BizcardResultParser.matchSingleDoCoMoPrefixedField("X:", rawText, true);
        String fullName = BizcardResultParser.buildName(firstName, lastName);
        String title = BizcardResultParser.matchSingleDoCoMoPrefixedField("T:", rawText, true);
        String org = BizcardResultParser.matchSingleDoCoMoPrefixedField("C:", rawText, true);
        String[] addresses = BizcardResultParser.matchDoCoMoPrefixedField("A:", rawText, true);
        String phoneNumber1 = BizcardResultParser.matchSingleDoCoMoPrefixedField("B:", rawText, true);
        String phoneNumber2 = BizcardResultParser.matchSingleDoCoMoPrefixedField("M:", rawText, true);
        String phoneNumber3 = BizcardResultParser.matchSingleDoCoMoPrefixedField("F:", rawText, true);
        String email = BizcardResultParser.matchSingleDoCoMoPrefixedField("E:", rawText, true);
        return new AddressBookParsedResult(BizcardResultParser.maybeWrap(fullName), null, null, BizcardResultParser.buildPhoneNumbers(phoneNumber1, phoneNumber2, phoneNumber3), null, BizcardResultParser.maybeWrap(email), null, null, null, addresses, null, org, null, title, null, null);
    }

    private static String[] buildPhoneNumbers(String number1, String number2, String number3) {
        int size;
        ArrayList<String> numbers = new ArrayList<String>(3);
        if (number1 != null) {
            numbers.add(number1);
        }
        if (number2 != null) {
            numbers.add(number2);
        }
        if (number3 != null) {
            numbers.add(number3);
        }
        if ((size = numbers.size()) == 0) {
            return null;
        }
        return numbers.toArray(new String[size]);
    }

    private static String buildName(String firstName, String lastName) {
        if (firstName == null) {
            return lastName;
        }
        return lastName == null ? firstName : firstName + ' ' + lastName;
    }
}

