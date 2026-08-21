/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing;

import com.google.zxing.StringsResourceTranslator;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

public final class HtmlAssetTranslator {
    private static final Pattern COMMA = Pattern.compile(",");

    private HtmlAssetTranslator() {
    }

    public static void main(String[] args2) throws IOException {
        if (args2.length < 3) {
            System.err.println("Usage: HtmlAssetTranslator android/assets/ (all|lang1[,lang2 ...]) (all|file1.html[ file2.html ...])");
            return;
        }
        Path assetsDir = Paths.get(args2[0], new String[0]);
        Collection<String> languagesToTranslate = HtmlAssetTranslator.parseLanguagesToTranslate(assetsDir, args2[1]);
        List<String> restOfArgs = Arrays.asList(args2).subList(2, args2.length);
        Collection<String> fileNamesToTranslate = HtmlAssetTranslator.parseFileNamesToTranslate(assetsDir, restOfArgs);
        for (String language : languagesToTranslate) {
            HtmlAssetTranslator.translateOneLanguage(assetsDir, language, fileNamesToTranslate);
        }
    }

    private static Collection<String> parseLanguagesToTranslate(Path assetsDir, CharSequence languageArg) throws IOException {
        if ("all".equals(languageArg)) {
            ArrayList<String> languages = new ArrayList<String>();
            DirectoryStream.Filter<Path> fileFilter = new DirectoryStream.Filter<Path>(){

                @Override
                public boolean accept(Path entry) {
                    String fileName = entry.getFileName().toString();
                    return Files.isDirectory(entry, new LinkOption[0]) && !Files.isSymbolicLink(entry) && fileName.startsWith("html-") && !"html-en".equals(fileName);
                }
            };
            try (DirectoryStream<Path> dirs = Files.newDirectoryStream(assetsDir, (DirectoryStream.Filter<? super Path>)fileFilter);){
                for (Path languageDir : dirs) {
                    languages.add(languageDir.getFileName().toString().substring(5));
                }
            }
            return languages;
        }
        return Arrays.asList(COMMA.split(languageArg));
    }

    private static Collection<String> parseFileNamesToTranslate(Path assetsDir, List<String> restOfArgs) throws IOException {
        if ("all".equals(restOfArgs.get(0))) {
            ArrayList<String> fileNamesToTranslate = new ArrayList<String>();
            Path htmlEnAssetDir = assetsDir.resolve("html-en");
            try (DirectoryStream<Path> files = Files.newDirectoryStream(htmlEnAssetDir, "*.html");){
                for (Path file : files) {
                    fileNamesToTranslate.add(file.getFileName().toString());
                }
            }
            return fileNamesToTranslate;
        }
        return restOfArgs;
    }

    private static void translateOneLanguage(Path assetsDir, String language, final Collection<String> filesToTranslate) throws IOException {
        Path targetHtmlDir = assetsDir.resolve("html-" + language);
        Files.createDirectories(targetHtmlDir, new FileAttribute[0]);
        Path englishHtmlDir = assetsDir.resolve("html-en");
        String translationTextTranslated = StringsResourceTranslator.translateString("Translated by Google Translate.", language);
        DirectoryStream.Filter<Path> filter = new DirectoryStream.Filter<Path>(){

            @Override
            public boolean accept(Path entry) {
                String name = entry.getFileName().toString();
                return name.endsWith(".html") && (filesToTranslate.isEmpty() || filesToTranslate.contains(name));
            }
        };
        try (DirectoryStream<Path> files = Files.newDirectoryStream(englishHtmlDir, (DirectoryStream.Filter<? super Path>)filter);){
            for (Path sourceFile : files) {
                HtmlAssetTranslator.translateOneFile(language, targetHtmlDir, sourceFile, translationTextTranslated);
            }
        }
    }

    private static void translateOneFile(String language, Path targetHtmlDir, Path sourceFile, String translationTextTranslated) throws IOException {
        DOMImplementationRegistry registry;
        Document document;
        Path destFile = targetHtmlDir.resolve(sourceFile.getFileName());
        DocumentBuilderFactory factory2 = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory2.newDocumentBuilder();
            document = builder.parse(sourceFile.toFile());
        }
        catch (ParserConfigurationException pce) {
            throw new IllegalStateException(pce);
        }
        catch (SAXException sae) {
            throw new IOException(sae);
        }
        Element rootElement = document.getDocumentElement();
        rootElement.normalize();
        LinkedList<Node> nodes = new LinkedList<Node>();
        nodes.add(rootElement);
        while (!nodes.isEmpty()) {
            String text;
            Node node = (Node)nodes.poll();
            if (HtmlAssetTranslator.shouldTranslate(node)) {
                NodeList children = node.getChildNodes();
                for (int i = 0; i < children.getLength(); ++i) {
                    nodes.add(children.item(i));
                }
            }
            if (node.getNodeType() != 3 || (text = node.getTextContent()).trim().isEmpty()) continue;
            text = StringsResourceTranslator.translateString(text, language);
            node.setTextContent(' ' + text + ' ');
        }
        Text translateText = document.createTextNode(translationTextTranslated);
        Element paragraph = document.createElement("p");
        paragraph.appendChild(translateText);
        Node body = rootElement.getElementsByTagName("body").item(0);
        body.appendChild(paragraph);
        try {
            registry = DOMImplementationRegistry.newInstance();
        }
        catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new IllegalStateException(e);
        }
        DOMImplementationLS impl = (DOMImplementationLS)((Object)registry.getDOMImplementation("LS"));
        LSSerializer writer = impl.createLSSerializer();
        String fileAsString = writer.writeToString(document);
        fileAsString = fileAsString.replaceAll("<\\?xml[^>]+>", "<!DOCTYPE HTML>");
        Files.write(destFile, Collections.singleton(fileAsString), StandardCharsets.UTF_8, new OpenOption[0]);
    }

    private static boolean shouldTranslate(Node node) {
        String textContent;
        Node classAttribute;
        NamedNodeMap attributes = node.getAttributes();
        if (attributes != null && (classAttribute = attributes.getNamedItem("class")) != null && (textContent = classAttribute.getTextContent()) != null && textContent.contains("notranslate")) {
            return false;
        }
        String nodeName = node.getNodeName();
        if ("script".equalsIgnoreCase(nodeName)) {
            return false;
        }
        textContent = node.getTextContent();
        if (textContent != null) {
            for (int i = 0; i < textContent.length(); ++i) {
                if (!Character.isLetter(textContent.charAt(i))) continue;
                return true;
            }
        }
        return false;
    }
}

