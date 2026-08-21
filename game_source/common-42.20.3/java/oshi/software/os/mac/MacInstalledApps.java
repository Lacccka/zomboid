/*
 * Decompiled with CFR 0.152.
 */
package oshi.software.os.mac;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.software.os.ApplicationInfo;
import oshi.util.ExecutingCommand;
import oshi.util.FileUtil;
import oshi.util.ParseUtil;

public final class MacInstalledApps {
    private static final Logger LOG = LoggerFactory.getLogger(MacInstalledApps.class);

    private MacInstalledApps() {
    }

    public static List<ApplicationInfo> queryInstalledApps() {
        List<String> output = ExecutingCommand.runNative("system_profiler -xml SPApplicationsDataType");
        try {
            List<Map<String, String>> plistValues = MacInstalledApps.parseItems(String.join((CharSequence)"", output));
            if (!plistValues.isEmpty()) {
                LinkedHashSet<ApplicationInfo> appInfoSet = new LinkedHashSet<ApplicationInfo>();
                for (Map<String, String> dictValues : plistValues) {
                    try {
                        File appPlistFile;
                        String obtainedFrom = ParseUtil.getStringValueOrUnknown(dictValues.get("obtained_from"));
                        if ("apple".equals(obtainedFrom)) {
                            obtainedFrom = "Apple";
                        } else if ("mac_app_store".equals(obtainedFrom)) {
                            obtainedFrom = "App Store";
                        }
                        String signedBy = ParseUtil.getStringValueOrUnknown(dictValues.get("signed_by"));
                        String vendor = "identified_developer".equals(obtainedFrom) ? (signedBy.startsWith("Developer ID Application: ") ? signedBy.substring(26) : signedBy) : ("unknown".equals(obtainedFrom) && !"unknown".equals(signedBy) ? signedBy : obtainedFrom);
                        String version = dictValues.get("version");
                        String lastModified = dictValues.get("lastModified");
                        long lastModifiedEpoch = ParseUtil.parseDateToEpoch(lastModified, "yyyy-MM-dd'T'HH:mm:ss'Z'");
                        LinkedHashMap<String, String> additionalInfo = new LinkedHashMap<String, String>();
                        additionalInfo.put("Kind", ParseUtil.getStringValueOrUnknown(dictValues.get("arch_kind")));
                        String location = ParseUtil.getStringValueOrUnknown(dictValues.get("path"));
                        additionalInfo.put("Location", location);
                        if (!"unknown".equals(location) && (appPlistFile = new File(location, "/Contents/Info.plist")).exists()) {
                            output = "bplist00".equals(new String(MacInstalledApps.readFirstBytes(appPlistFile), StandardCharsets.UTF_8)) ? ExecutingCommand.runNative(new String[]{"plutil", "-convert", "xml1", "-o", "-", appPlistFile.getAbsolutePath()}) : FileUtil.readFile(appPlistFile.getAbsolutePath());
                            String xml = String.join((CharSequence)"", output);
                            String getInfoString = MacInstalledApps.readStringValue(xml, "CFBundleGetInfoString");
                            if (getInfoString != null && !getInfoString.isEmpty()) {
                                additionalInfo.put("Get Info String", getInfoString);
                            }
                            if (version == null || version.isEmpty()) {
                                version = MacInstalledApps.readStringValue(xml, "CFBundleVersion");
                            }
                        }
                        if (version == null || version.isEmpty()) {
                            version = "unknown";
                        }
                        appInfoSet.add(new ApplicationInfo(dictValues.get("_name"), version, vendor, lastModifiedEpoch, additionalInfo));
                    }
                    catch (Exception e) {
                        LOG.trace("Unable to parse dict values: " + e.getMessage() + " - " + String.valueOf(dictValues), e);
                    }
                }
                return new ArrayList<ApplicationInfo>(appInfoSet);
            }
        }
        catch (Exception e) {
            LOG.trace("Unable to read installed apps: " + e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    private static byte[] readFirstBytes(File file) throws IOException {
        byte[] buffer = new byte[8];
        try (FileInputStream fis = new FileInputStream(file);){
            fis.read(buffer);
            byte[] byArray = buffer;
            return byArray;
        }
    }

    private static List<Map<String, String>> parseItems(String xml) {
        if (xml == null) {
            return Collections.emptyList();
        }
        Matcher m = Pattern.compile("<key>\\s*_items\\s*</key>").matcher(xml);
        if (!m.find()) {
            return Collections.emptyList();
        }
        int arrayOpen = xml.indexOf("<array>", m.end());
        if (arrayOpen < 0) {
            return Collections.emptyList();
        }
        String arrayBody = MacInstalledApps.extractBalancedInner(xml, arrayOpen, "<array>", "</array>");
        if (arrayBody == null) {
            return Collections.emptyList();
        }
        List<String> dictBlocks = MacInstalledApps.extractTopLevelBlocks(arrayBody, "<dict>", "</dict>");
        ArrayList<Map<String, String>> out = new ArrayList<Map<String, String>>();
        for (String dictInner : dictBlocks) {
            out.add(MacInstalledApps.parseDict(dictInner));
        }
        return out;
    }

    private static Map<String, String> parseDict(String dictInner) {
        int kEnd;
        int kStart;
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        int pos = 0;
        while ((kStart = dictInner.indexOf("<key>", pos)) >= 0 && (kEnd = dictInner.indexOf("</key>", kStart + 5)) >= 0) {
            String val;
            String key = MacInstalledApps.unescape(dictInner.substring(kStart + 5, kEnd).trim());
            int vOpen = dictInner.indexOf(60, kEnd + 6);
            if (vOpen < 0) break;
            if (MacInstalledApps.startsWith(dictInner, vOpen, "<string>")) {
                inner = MacInstalledApps.extractSimpleInner(dictInner, vOpen, "<string>", "</string>");
                val = MacInstalledApps.unescape(inner);
                pos = dictInner.indexOf("</string>", vOpen) + "</string>".length();
            } else if (MacInstalledApps.startsWith(dictInner, vOpen, "<date>")) {
                inner = MacInstalledApps.extractSimpleInner(dictInner, vOpen, "<date>", "</date>");
                val = inner.trim();
                pos = dictInner.indexOf("</date>", vOpen) + "</date>".length();
            } else if (MacInstalledApps.startsWith(dictInner, vOpen, "<array>")) {
                inner = MacInstalledApps.extractBalancedInner(dictInner, vOpen, "<array>", "</array>");
                String parsed = inner == null ? null : MacInstalledApps.parseStringArray(inner);
                val = parsed == null ? "" : parsed;
                pos = dictInner.indexOf("</array>", vOpen) + "</array>".length();
            } else {
                pos = vOpen + 1;
                continue;
            }
            map.put(key, val);
        }
        return map;
    }

    private static String parseStringArray(String arrayInner) {
        int lt = arrayInner.indexOf(60);
        if (lt >= 0 && MacInstalledApps.startsWith(arrayInner, lt, "<string>")) {
            String inner = MacInstalledApps.extractSimpleInner(arrayInner, lt, "<string>", "</string>");
            return MacInstalledApps.unescape(inner);
        }
        return null;
    }

    private static boolean startsWith(String s, int pos, String tag) {
        int end = pos + tag.length();
        return end <= s.length() && s.regionMatches(false, pos, tag, 0, tag.length());
    }

    private static String extractSimpleInner(String s, int openPos, String openTag, String closeTag) {
        int start = s.indexOf(openTag, openPos);
        if (start < 0) {
            return "";
        }
        int end = s.indexOf(closeTag, start + openTag.length());
        if (end < 0) {
            return "";
        }
        return s.substring(start + openTag.length(), end);
    }

    private static String extractBalancedInner(String s, int openPos, String openTag, String closeTag) {
        int pos = openPos;
        if (!MacInstalledApps.startsWith(s, pos, openTag)) {
            return null;
        }
        pos += openTag.length();
        int depth = 1;
        while (pos < s.length()) {
            int nextOpen = s.indexOf(openTag, pos);
            int nextClose = s.indexOf(closeTag, pos);
            if (nextClose == -1) {
                return null;
            }
            if (nextOpen != -1 && nextOpen < nextClose) {
                ++depth;
                pos = nextOpen + openTag.length();
                continue;
            }
            if (--depth == 0) {
                return s.substring(openPos + openTag.length(), nextClose);
            }
            pos = nextClose + closeTag.length();
        }
        return null;
    }

    private static List<String> extractTopLevelBlocks(String containerInner, String openTag, String closeTag) {
        String inner;
        int open;
        ArrayList<String> blocks = new ArrayList<String>();
        int pos = 0;
        while ((open = containerInner.indexOf(openTag, pos)) >= 0 && (inner = MacInstalledApps.extractBalancedInner(containerInner, open, openTag, closeTag)) != null) {
            int closeEnd;
            blocks.add(inner);
            pos = closeEnd = containerInner.indexOf(closeTag, open) + closeTag.length();
        }
        return blocks;
    }

    private static String unescape(String s) {
        return s.replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&apos;", "'");
    }

    private static String readStringValue(String xml, String keyName) throws Exception {
        int e;
        int i = xml.indexOf("<key>" + keyName + "</key>");
        if (i > 0 && (i = xml.indexOf("<string>", i)) > 0 && (e = xml.indexOf("</string>", i += 8)) > 0) {
            return xml.substring(i, e);
        }
        return null;
    }
}

