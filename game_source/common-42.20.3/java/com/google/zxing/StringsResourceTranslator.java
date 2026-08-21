/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.URI;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StringsResourceTranslator {
    private static final String API_KEY = System.getProperty("translateAPI.key");
    private static final Pattern ENTRY_PATTERN;
    private static final Pattern STRINGS_FILE_NAME_PATTERN;
    private static final Pattern TRANSLATE_RESPONSE_PATTERN;
    private static final Pattern VALUES_DIR_PATTERN;
    private static final String APACHE_2_LICENSE = "<!--\n Copyright (C) 2015 ZXing authors\n\n Licensed under the Apache License, Version 2.0 (the \"License\");\n you may not use this file except in compliance with the License.\n You may obtain a copy of the License at\n\n      http://www.apache.org/licenses/LICENSE-2.0\n\n Unless required by applicable law or agreed to in writing, software\n distributed under the License is distributed on an \"AS IS\" BASIS,\n WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n See the License for the specific language governing permissions and\n limitations under the License.\n -->\n";
    private static final Map<String, String> LANGUAGE_CODE_MASSAGINGS;

    private StringsResourceTranslator() {
    }

    public static void main(String[] args2) throws IOException {
        Path resDir = Paths.get(args2[0], new String[0]);
        Path valueDir = resDir.resolve("values");
        Path stringsFile = valueDir.resolve("strings.xml");
        List<String> forceRetranslation = Arrays.asList(args2).subList(1, args2.length);
        DirectoryStream.Filter<Path> filter = new DirectoryStream.Filter<Path>(){

            @Override
            public boolean accept(Path entry) {
                return Files.isDirectory(entry, new LinkOption[0]) && !Files.isSymbolicLink(entry) && VALUES_DIR_PATTERN.matcher(entry.getFileName().toString()).matches();
            }
        };
        try (DirectoryStream<Path> dirs = Files.newDirectoryStream(resDir, (DirectoryStream.Filter<? super Path>)filter);){
            for (Path dir : dirs) {
                StringsResourceTranslator.translate(stringsFile, dir.resolve("strings.xml"), forceRetranslation);
            }
        }
    }

    private static void translate(Path englishFile, Path translatedFile, Collection<String> forceRetranslation) throws IOException {
        Map<String, String> english = StringsResourceTranslator.readLines(englishFile);
        Map<String, String> translated = StringsResourceTranslator.readLines(translatedFile);
        String parentName = translatedFile.getParent().getFileName().toString();
        Matcher stringsFileNameMatcher = STRINGS_FILE_NAME_PATTERN.matcher(parentName);
        if (!stringsFileNameMatcher.find()) {
            throw new IllegalArgumentException("Invalid parent dir: " + parentName);
        }
        String language = stringsFileNameMatcher.group(1);
        String massagedLanguage = LANGUAGE_CODE_MASSAGINGS.get(language);
        if (massagedLanguage != null) {
            language = massagedLanguage;
        }
        System.out.println("Translating " + language);
        Path resultTempFile = Files.createTempFile(null, null, new FileAttribute[0]);
        boolean anyChange = false;
        try (BufferedWriter out = Files.newBufferedWriter(resultTempFile, StandardCharsets.UTF_8, new OpenOption[0]);){
            out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            out.write(APACHE_2_LICENSE);
            out.write("<resources>\n");
            for (Map.Entry<String, String> englishEntry : english.entrySet()) {
                String key = englishEntry.getKey();
                String value = englishEntry.getValue();
                out.write("  <string name=\"");
                out.write(key);
                ((Writer)out).write(34);
                if (value.contains("%s") || value.contains("%f")) {
                    out.write(" formatted=\"false\"");
                }
                ((Writer)out).write(62);
                String translatedString = translated.get(key);
                if (translatedString == null || forceRetranslation.contains(key)) {
                    anyChange = true;
                    translatedString = StringsResourceTranslator.translateString(value, language);
                    translatedString = translatedString.replaceAll("'", "\\\\'");
                }
                out.write(translatedString);
                out.write("</string>\n");
            }
            out.write("</resources>\n");
            ((Writer)out).flush();
        }
        if (anyChange) {
            System.out.println("  Writing translations");
            Files.move(resultTempFile, translatedFile, StandardCopyOption.REPLACE_EXISTING);
        } else {
            Files.delete(resultTempFile);
        }
    }

    static String translateString(String english, String language) throws IOException {
        if ("en".equals(language)) {
            return english;
        }
        String massagedLanguage = LANGUAGE_CODE_MASSAGINGS.get(language);
        if (massagedLanguage != null) {
            language = massagedLanguage;
        }
        System.out.println("  Need translation for " + english);
        URI translateURI = URI.create("https://www.googleapis.com/language/translate/v2?key=" + API_KEY + "&q=" + URLEncoder.encode(english, "UTF-8") + "&source=en&target=" + language);
        CharSequence translateResult = StringsResourceTranslator.fetch(translateURI);
        Matcher m = TRANSLATE_RESPONSE_PATTERN.matcher(translateResult);
        if (!m.find()) {
            System.err.println("No translate result");
            System.err.println(translateResult);
            return english;
        }
        String translation = m.group(1);
        translation = translation.replaceAll("&(amp;)?quot;", "\"");
        translation = translation.replaceAll("&(amp;)?#39;", "'");
        System.out.println("  Got translation " + translation);
        return translation;
    }

    private static CharSequence fetch(URI translateURI) throws IOException {
        URLConnection connection = translateURI.toURL().openConnection();
        connection.connect();
        StringBuilder translateResult = new StringBuilder(200);
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));){
            int charsRead;
            char[] buffer = new char[8192];
            while ((charsRead = in.read(buffer)) > 0) {
                translateResult.append(buffer, 0, charsRead);
            }
        }
        return translateResult;
    }

    private static Map<String, String> readLines(Path file) throws IOException {
        if (Files.exists(file, new LinkOption[0])) {
            TreeMap<String, String> entries = new TreeMap<String, String>();
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                Matcher m = ENTRY_PATTERN.matcher(line);
                if (!m.find()) continue;
                entries.put(m.group(1), m.group(2));
            }
            return entries;
        }
        return Collections.emptyMap();
    }

    static {
        if (API_KEY == null) {
            throw new IllegalArgumentException("translateAPI.key is not specified");
        }
        ENTRY_PATTERN = Pattern.compile("<string name=\"([^\"]+)\".*>([^<]+)</string>");
        STRINGS_FILE_NAME_PATTERN = Pattern.compile("values-(.+)");
        TRANSLATE_RESPONSE_PATTERN = Pattern.compile("translatedText\":\\s*\"([^\"]+)\"");
        VALUES_DIR_PATTERN = Pattern.compile("values-[a-z]{2}(-[a-zA-Z]{2,3})?");
        LANGUAGE_CODE_MASSAGINGS = new HashMap<String, String>(3);
        LANGUAGE_CODE_MASSAGINGS.put("zh-rCN", "zh-cn");
        LANGUAGE_CODE_MASSAGINGS.put("zh-rTW", "zh-tw");
    }
}

