/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.api.impl;

import com.sun.xml.bind.api.impl.NameUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.lang.model.SourceVersion;

public interface NameConverter {
    public static final NameConverter standard = new Standard();
    public static final NameConverter jaxrpcCompatible = new Standard(){

        @Override
        protected boolean isPunct(char c) {
            return c == '.' || c == '-' || c == ';' || c == '\u00b7' || c == '\u0387' || c == '\u06dd' || c == '\u06de';
        }

        @Override
        protected boolean isLetter(char c) {
            return super.isLetter(c) || c == '_';
        }

        @Override
        protected int classify(char c0) {
            if (c0 == '_') {
                return 2;
            }
            return super.classify(c0);
        }
    };
    public static final NameConverter smart = new Standard(){

        @Override
        public String toConstantName(String token) {
            String name = super.toConstantName(token);
            if (!SourceVersion.isKeyword(name)) {
                return name;
            }
            return '_' + name;
        }
    };

    public String toClassName(String var1);

    public String toInterfaceName(String var1);

    public String toPropertyName(String var1);

    public String toConstantName(String var1);

    public String toVariableName(String var1);

    public String toPackageName(String var1);

    public static class Standard
    extends NameUtil
    implements NameConverter {
        @Override
        public String toClassName(String s) {
            return this.toMixedCaseName(this.toWordList(s), true);
        }

        @Override
        public String toVariableName(String s) {
            return this.toMixedCaseName(this.toWordList(s), false);
        }

        @Override
        public String toInterfaceName(String token) {
            return this.toClassName(token);
        }

        @Override
        public String toPropertyName(String s) {
            String prop = this.toClassName(s);
            if (prop.equals("Class")) {
                prop = "Clazz";
            }
            return prop;
        }

        @Override
        public String toConstantName(String token) {
            return super.toConstantName(token);
        }

        @Override
        public String toPackageName(String nsUri) {
            ArrayList<String> r;
            String domain;
            String lastToken;
            ArrayList<String> tokens;
            int idx = nsUri.indexOf(58);
            String scheme = "";
            if (idx >= 0 && ((scheme = nsUri.substring(0, idx)).equalsIgnoreCase("http") || scheme.equalsIgnoreCase("urn"))) {
                nsUri = nsUri.substring(idx + 1);
            }
            if ((tokens = Standard.tokenize(nsUri, "/: ")).size() == 0) {
                return null;
            }
            if (tokens.size() > 1 && (idx = (lastToken = tokens.get(tokens.size() - 1)).lastIndexOf(46)) > 0) {
                lastToken = lastToken.substring(0, idx);
                tokens.set(tokens.size() - 1, lastToken);
            }
            if ((idx = (domain = tokens.get(0)).indexOf(58)) >= 0) {
                domain = domain.substring(0, idx);
            }
            if ((r = Standard.reverse(Standard.tokenize(domain, scheme.equals("urn") ? ".-" : "."))).get(r.size() - 1).equalsIgnoreCase("www")) {
                r.remove(r.size() - 1);
            }
            tokens.addAll(1, r);
            tokens.remove(0);
            for (int i = 0; i < tokens.size(); ++i) {
                String token = tokens.get(i);
                if (SourceVersion.isKeyword((token = Standard.removeIllegalIdentifierChars(token)).toLowerCase())) {
                    token = '_' + token;
                }
                tokens.set(i, token.toLowerCase());
            }
            return Standard.combine(tokens, '.');
        }

        private static String removeIllegalIdentifierChars(String token) {
            StringBuilder newToken = new StringBuilder(token.length() + 1);
            for (int i = 0; i < token.length(); ++i) {
                char c = token.charAt(i);
                if (i == 0 && !Character.isJavaIdentifierStart(c)) {
                    newToken.append('_');
                }
                if (!Character.isJavaIdentifierPart(c)) {
                    newToken.append('_');
                    continue;
                }
                newToken.append(c);
            }
            return newToken.toString();
        }

        private static ArrayList<String> tokenize(String str, String sep) {
            StringTokenizer tokens = new StringTokenizer(str, sep);
            ArrayList<String> r = new ArrayList<String>();
            while (tokens.hasMoreTokens()) {
                r.add(tokens.nextToken());
            }
            return r;
        }

        private static <T> ArrayList<T> reverse(List<T> a) {
            ArrayList<T> r = new ArrayList<T>();
            for (int i = a.size() - 1; i >= 0; --i) {
                r.add(a.get(i));
            }
            return r;
        }

        private static String combine(List r, char sep) {
            StringBuilder buf = new StringBuilder(r.get(0).toString());
            for (int i = 1; i < r.size(); ++i) {
                buf.append(sep);
                buf.append(r.get(i));
            }
            return buf.toString();
        }
    }
}

