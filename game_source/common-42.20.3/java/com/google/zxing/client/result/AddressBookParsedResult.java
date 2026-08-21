/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.client.result;

import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ParsedResultType;

public final class AddressBookParsedResult
extends ParsedResult {
    private final String[] names;
    private final String[] nicknames;
    private final String pronunciation;
    private final String[] phoneNumbers;
    private final String[] phoneTypes;
    private final String[] emails;
    private final String[] emailTypes;
    private final String instantMessenger;
    private final String note;
    private final String[] addresses;
    private final String[] addressTypes;
    private final String org;
    private final String birthday;
    private final String title;
    private final String[] urls;
    private final String[] geo;

    public AddressBookParsedResult(String[] names, String[] phoneNumbers, String[] phoneTypes, String[] emails, String[] emailTypes, String[] addresses, String[] addressTypes) {
        this(names, null, null, phoneNumbers, phoneTypes, emails, emailTypes, null, null, addresses, addressTypes, null, null, null, null, null);
    }

    public AddressBookParsedResult(String[] names, String[] nicknames, String pronunciation, String[] phoneNumbers, String[] phoneTypes, String[] emails, String[] emailTypes, String instantMessenger, String note, String[] addresses, String[] addressTypes, String org, String birthday, String title, String[] urls2, String[] geo) {
        super(ParsedResultType.ADDRESSBOOK);
        this.names = names;
        this.nicknames = nicknames;
        this.pronunciation = pronunciation;
        this.phoneNumbers = phoneNumbers;
        this.phoneTypes = phoneTypes;
        this.emails = emails;
        this.emailTypes = emailTypes;
        this.instantMessenger = instantMessenger;
        this.note = note;
        this.addresses = addresses;
        this.addressTypes = addressTypes;
        this.org = org;
        this.birthday = birthday;
        this.title = title;
        this.urls = urls2;
        this.geo = geo;
    }

    public String[] getNames() {
        return this.names;
    }

    public String[] getNicknames() {
        return this.nicknames;
    }

    public String getPronunciation() {
        return this.pronunciation;
    }

    public String[] getPhoneNumbers() {
        return this.phoneNumbers;
    }

    public String[] getPhoneTypes() {
        return this.phoneTypes;
    }

    public String[] getEmails() {
        return this.emails;
    }

    public String[] getEmailTypes() {
        return this.emailTypes;
    }

    public String getInstantMessenger() {
        return this.instantMessenger;
    }

    public String getNote() {
        return this.note;
    }

    public String[] getAddresses() {
        return this.addresses;
    }

    public String[] getAddressTypes() {
        return this.addressTypes;
    }

    public String getTitle() {
        return this.title;
    }

    public String getOrg() {
        return this.org;
    }

    public String[] getURLs() {
        return this.urls;
    }

    public String getBirthday() {
        return this.birthday;
    }

    public String[] getGeo() {
        return this.geo;
    }

    @Override
    public String getDisplayResult() {
        StringBuilder result = new StringBuilder(100);
        AddressBookParsedResult.maybeAppend(this.names, result);
        AddressBookParsedResult.maybeAppend(this.nicknames, result);
        AddressBookParsedResult.maybeAppend(this.pronunciation, result);
        AddressBookParsedResult.maybeAppend(this.title, result);
        AddressBookParsedResult.maybeAppend(this.org, result);
        AddressBookParsedResult.maybeAppend(this.addresses, result);
        AddressBookParsedResult.maybeAppend(this.phoneNumbers, result);
        AddressBookParsedResult.maybeAppend(this.emails, result);
        AddressBookParsedResult.maybeAppend(this.instantMessenger, result);
        AddressBookParsedResult.maybeAppend(this.urls, result);
        AddressBookParsedResult.maybeAppend(this.birthday, result);
        AddressBookParsedResult.maybeAppend(this.geo, result);
        AddressBookParsedResult.maybeAppend(this.note, result);
        return result.toString();
    }
}

