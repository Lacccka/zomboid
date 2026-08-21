/*
 * Decompiled with CFR 0.152.
 */
package org.json;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.json.ParserConfiguration;
import org.json.XMLXsiTypeConverter;

public class XMLParserConfiguration
extends ParserConfiguration {
    private boolean keepNumberAsString;
    private boolean keepBooleanAsString;
    public static final XMLParserConfiguration ORIGINAL = new XMLParserConfiguration();
    public static final XMLParserConfiguration KEEP_STRINGS = new XMLParserConfiguration().withKeepStrings(true);
    private String cDataTagName;
    private boolean convertNilAttributeToNull;
    private boolean closeEmptyTag;
    private Map<String, XMLXsiTypeConverter<?>> xsiTypeMap;
    private Set<String> forceList;
    private boolean shouldTrimWhiteSpace;

    public XMLParserConfiguration() {
        this.cDataTagName = "content";
        this.convertNilAttributeToNull = false;
        this.xsiTypeMap = Collections.emptyMap();
        this.forceList = Collections.emptySet();
        this.shouldTrimWhiteSpace = true;
    }

    @Deprecated
    public XMLParserConfiguration(boolean keepStrings) {
        this(keepStrings, "content", false);
    }

    @Deprecated
    public XMLParserConfiguration(String cDataTagName) {
        this(false, cDataTagName, false);
    }

    @Deprecated
    public XMLParserConfiguration(boolean keepStrings, String cDataTagName) {
        super(keepStrings, 512);
        this.cDataTagName = cDataTagName;
        this.convertNilAttributeToNull = false;
    }

    @Deprecated
    public XMLParserConfiguration(boolean keepStrings, String cDataTagName, boolean convertNilAttributeToNull) {
        super(false, 512);
        this.keepNumberAsString = keepStrings;
        this.keepBooleanAsString = keepStrings;
        this.cDataTagName = cDataTagName;
        this.convertNilAttributeToNull = convertNilAttributeToNull;
    }

    private XMLParserConfiguration(boolean keepStrings, String cDataTagName, boolean convertNilAttributeToNull, Map<String, XMLXsiTypeConverter<?>> xsiTypeMap, Set<String> forceList, int maxNestingDepth, boolean closeEmptyTag, boolean keepNumberAsString, boolean keepBooleanAsString) {
        super(false, maxNestingDepth);
        this.keepNumberAsString = keepNumberAsString;
        this.keepBooleanAsString = keepBooleanAsString;
        this.cDataTagName = cDataTagName;
        this.convertNilAttributeToNull = convertNilAttributeToNull;
        this.xsiTypeMap = Collections.unmodifiableMap(xsiTypeMap);
        this.forceList = Collections.unmodifiableSet(forceList);
        this.closeEmptyTag = closeEmptyTag;
    }

    @Override
    protected XMLParserConfiguration clone() {
        XMLParserConfiguration config = new XMLParserConfiguration(this.keepStrings, this.cDataTagName, this.convertNilAttributeToNull, this.xsiTypeMap, this.forceList, this.maxNestingDepth, this.closeEmptyTag, this.keepNumberAsString, this.keepBooleanAsString);
        config.shouldTrimWhiteSpace = this.shouldTrimWhiteSpace;
        return config;
    }

    public XMLParserConfiguration withKeepStrings(boolean newVal) {
        XMLParserConfiguration newConfig = this.clone();
        newConfig.keepStrings = newVal;
        newConfig.keepNumberAsString = newVal;
        newConfig.keepBooleanAsString = newVal;
        return newConfig;
    }

    public XMLParserConfiguration withKeepNumberAsString(boolean newVal) {
        XMLParserConfiguration newConfig = this.clone();
        newConfig.keepNumberAsString = newVal;
        newConfig.keepStrings = newConfig.keepBooleanAsString && newConfig.keepNumberAsString;
        return newConfig;
    }

    public XMLParserConfiguration withKeepBooleanAsString(boolean newVal) {
        XMLParserConfiguration newConfig = this.clone();
        newConfig.keepBooleanAsString = newVal;
        newConfig.keepStrings = newConfig.keepBooleanAsString && newConfig.keepNumberAsString;
        return newConfig;
    }

    public String getcDataTagName() {
        return this.cDataTagName;
    }

    public boolean isKeepNumberAsString() {
        return this.keepNumberAsString;
    }

    public boolean isKeepBooleanAsString() {
        return this.keepBooleanAsString;
    }

    public XMLParserConfiguration withcDataTagName(String newVal) {
        XMLParserConfiguration newConfig = this.clone();
        newConfig.cDataTagName = newVal;
        return newConfig;
    }

    public boolean isConvertNilAttributeToNull() {
        return this.convertNilAttributeToNull;
    }

    public XMLParserConfiguration withConvertNilAttributeToNull(boolean newVal) {
        XMLParserConfiguration newConfig = this.clone();
        newConfig.convertNilAttributeToNull = newVal;
        return newConfig;
    }

    public Map<String, XMLXsiTypeConverter<?>> getXsiTypeMap() {
        return this.xsiTypeMap;
    }

    public XMLParserConfiguration withXsiTypeMap(Map<String, XMLXsiTypeConverter<?>> xsiTypeMap) {
        XMLParserConfiguration newConfig = this.clone();
        HashMap cloneXsiTypeMap = new HashMap(xsiTypeMap);
        newConfig.xsiTypeMap = Collections.unmodifiableMap(cloneXsiTypeMap);
        return newConfig;
    }

    public Set<String> getForceList() {
        return this.forceList;
    }

    public XMLParserConfiguration withForceList(Set<String> forceList) {
        XMLParserConfiguration newConfig = this.clone();
        HashSet<String> cloneForceList = new HashSet<String>(forceList);
        newConfig.forceList = Collections.unmodifiableSet(cloneForceList);
        return newConfig;
    }

    public XMLParserConfiguration withMaxNestingDepth(int maxNestingDepth) {
        return (XMLParserConfiguration)super.withMaxNestingDepth(maxNestingDepth);
    }

    public XMLParserConfiguration withCloseEmptyTag(boolean closeEmptyTag) {
        XMLParserConfiguration clonedConfiguration = this.clone();
        clonedConfiguration.closeEmptyTag = closeEmptyTag;
        return clonedConfiguration;
    }

    public XMLParserConfiguration withShouldTrimWhitespace(boolean shouldTrimWhiteSpace) {
        XMLParserConfiguration clonedConfiguration = this.clone();
        clonedConfiguration.shouldTrimWhiteSpace = shouldTrimWhiteSpace;
        return clonedConfiguration;
    }

    public boolean isCloseEmptyTag() {
        return this.closeEmptyTag;
    }

    public boolean shouldTrimWhiteSpace() {
        return this.shouldTrimWhiteSpace;
    }
}

