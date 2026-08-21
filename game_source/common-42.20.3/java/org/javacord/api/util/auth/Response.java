/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.util.auth;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.javacord.api.util.auth.Challenge;

public interface Response {
    public static final String TOKEN_PATTERN_PART = "[!#$%&'*+.^_`|~\\p{Alnum}-]+";
    public static final String TOKEN68_PATTERN_PART = "[\\p{Alnum}._~+/-]+=*";
    public static final String OWS_PATTERN_PART = "[ \\t]*";
    public static final String QUOTED_PAIR_PATTERN_PART = "\\\\([\\t \\p{Graph}\\x80-\\xFF])";
    public static final String QUOTED_STRING_PATTERN_PART = "\"(?:[\\t \\x21\\x23-\\x5B\\x5D-\\x7E\\x80-\\xFF]|\\\\([\\t \\p{Graph}\\x80-\\xFF]))*\"";
    public static final String AUTH_PARAM_PATTERN_PART = "[!#$%&'*+.^_`|~\\p{Alnum}-]+[ \\t]*=[ \\t]*(?:[!#$%&'*+.^_`|~\\p{Alnum}-]+|\"(?:[\\t \\x21\\x23-\\x5B\\x5D-\\x7E\\x80-\\xFF]|\\\\([\\t \\p{Graph}\\x80-\\xFF]))*\")";
    public static final String CHALLENGE_PATTERN_PART = "[!#$%&'*+.^_`|~\\p{Alnum}-]+(?: +(?:[\\p{Alnum}._~+/-]+=*|(?:,|[!#$%&'*+.^_`|~\\p{Alnum}-]+[ \\t]*=[ \\t]*(?:[!#$%&'*+.^_`|~\\p{Alnum}-]+|\"(?:[\\t \\x21\\x23-\\x5B\\x5D-\\x7E\\x80-\\xFF]|\\\\([\\t \\p{Graph}\\x80-\\xFF]))*\"))(?:[ \\t]*,(?:[ \\t]*[!#$%&'*+.^_`|~\\p{Alnum}-]+[ \\t]*=[ \\t]*(?:[!#$%&'*+.^_`|~\\p{Alnum}-]+|\"(?:[\\t \\x21\\x23-\\x5B\\x5D-\\x7E\\x80-\\xFF]|\\\\([\\t \\p{Graph}\\x80-\\xFF]))*\"))?)*)?)?";
    public static final String AUTHENTICATION_HEADER_VALUE_SPLIT_PATTERN_PART = "(?:[ \\t]*,[ \\t]*)+";
    public static final Pattern AUTHENTICATION_HEADER_VALUE_PATTERN = Pattern.compile("^(?:,[ \\t]*)*[!#$%&'*+.^_`|~\\p{Alnum}-]+(?: +(?:[\\p{Alnum}._~+/-]+=*|(?:,|[!#$%&'*+.^_`|~\\p{Alnum}-]+[ \\t]*=[ \\t]*(?:[!#$%&'*+.^_`|~\\p{Alnum}-]+|\"(?:[\\t \\x21\\x23-\\x5B\\x5D-\\x7E\\x80-\\xFF]|\\\\([\\t \\p{Graph}\\x80-\\xFF]))*\"))(?:[ \\t]*,(?:[ \\t]*[!#$%&'*+.^_`|~\\p{Alnum}-]+[ \\t]*=[ \\t]*(?:[!#$%&'*+.^_`|~\\p{Alnum}-]+|\"(?:[\\t \\x21\\x23-\\x5B\\x5D-\\x7E\\x80-\\xFF]|\\\\([\\t \\p{Graph}\\x80-\\xFF]))*\"))?)*)?)?(?:[ \\t]*,(?:[ \\t]*[!#$%&'*+.^_`|~\\p{Alnum}-]+(?: +(?:[\\p{Alnum}._~+/-]+=*|(?:,|[!#$%&'*+.^_`|~\\p{Alnum}-]+[ \\t]*=[ \\t]*(?:[!#$%&'*+.^_`|~\\p{Alnum}-]+|\"(?:[\\t \\x21\\x23-\\x5B\\x5D-\\x7E\\x80-\\xFF]|\\\\([\\t \\p{Graph}\\x80-\\xFF]))*\"))(?:[ \\t]*,(?:[ \\t]*[!#$%&'*+.^_`|~\\p{Alnum}-]+[ \\t]*=[ \\t]*(?:[!#$%&'*+.^_`|~\\p{Alnum}-]+|\"(?:[\\t \\x21\\x23-\\x5B\\x5D-\\x7E\\x80-\\xFF]|\\\\([\\t \\p{Graph}\\x80-\\xFF]))*\"))?)*)?)?)?)*$");
    public static final Pattern AUTH_SCHEME_PATTERN = Pattern.compile("^[!#$%&'*+.^_`|~\\p{Alnum}-]+$");
    public static final Pattern AUTH_SCHEME_AND_TOKEN68_PATTERN = Pattern.compile("^[!#$%&'*+.^_`|~\\p{Alnum}-]+ +[\\p{Alnum}._~+/-]+=*$");
    public static final Pattern AUTH_SCHEME_AND_PARAM_PATTERN = Pattern.compile("^[!#$%&'*+.^_`|~\\p{Alnum}-]+ +[!#$%&'*+.^_`|~\\p{Alnum}-]+[ \\t]*=[ \\t]*(?:[!#$%&'*+.^_`|~\\p{Alnum}-]+|\"(?:[\\t \\x21\\x23-\\x5B\\x5D-\\x7E\\x80-\\xFF]|\\\\([\\t \\p{Graph}\\x80-\\xFF]))*\")$");
    public static final Pattern AUTH_PARAM_PATTERN = Pattern.compile("^[!#$%&'*+.^_`|~\\p{Alnum}-]+[ \\t]*=[ \\t]*(?:[!#$%&'*+.^_`|~\\p{Alnum}-]+|\"(?:[\\t \\x21\\x23-\\x5B\\x5D-\\x7E\\x80-\\xFF]|\\\\([\\t \\p{Graph}\\x80-\\xFF]))*\")$");
    public static final Pattern TOKEN_PATTERN = Pattern.compile("^[!#$%&'*+.^_`|~\\p{Alnum}-]+$");
    public static final Pattern QUOTED_PAIR_PATTERN = Pattern.compile("\\\\([\\t \\p{Graph}\\x80-\\xFF])");
    public static final Pattern AUTHENTICATION_HEADER_VALUE_SPLIT_PATTERN = Pattern.compile("(?:[ \\t]*,[ \\t]*)+");
    public static final Pattern WHITESPACE_SPLIT_PATTERN = Pattern.compile(" +");
    public static final Pattern AUTH_PARAM_SPLIT_PATTERN = Pattern.compile("[ \\t]*=[ \\t]*");
    public static final Pattern QUOTED_STRING_AUTH_PARAM_AT_END_PATTERN = Pattern.compile("[!#$%&'*+.^_`|~\\p{Alnum}-]+[ \\t]*=[ \\t]*\"(?:[\\t \\x21\\x23-\\x5B\\x5D-\\x7E\\x80-\\xFF]|\\\\([\\t \\p{Graph}\\x80-\\xFF]))*\"$");
    public static final Map<Map.Entry<String, String>, List<Challenge>> CHALLENGES_CACHE = new ConcurrentHashMap<Map.Entry<String, String>, List<Challenge>>();

    public int getCode();

    public String getMessage();

    public Map<String, List<String>> getHeaders();

    default public List<String> getHeaders(String headerName) {
        return this.getHeaders().get(headerName);
    }

    default public Optional<String> getBody() throws IOException {
        return Optional.empty();
    }

    default public Stream<Challenge> getChallenges() {
        return this.getChallenges(null);
    }

    default public Stream<Challenge> getChallenges(String authenticationScheme) {
        String authenticationHeader;
        switch (this.getCode()) {
            case 407: {
                authenticationHeader = "Proxy-Authenticate";
                break;
            }
            case 401: {
                authenticationHeader = "WWW-Authenticate";
                break;
            }
            default: {
                return Stream.empty();
            }
        }
        return this.getHeaders(authenticationHeader).stream().flatMap(authenticationHeaderValue -> {
            AbstractMap.SimpleEntry<String, Object> cacheKey = new AbstractMap.SimpleEntry<String, Object>((String)authenticationHeaderValue, (authenticationScheme == null ? null : authenticationScheme.toLowerCase(Locale.US)));
            return CHALLENGES_CACHE.computeIfAbsent(cacheKey, s -> {
                if (!AUTHENTICATION_HEADER_VALUE_PATTERN.matcher((CharSequence)authenticationHeaderValue).matches()) {
                    return Collections.emptyList();
                }
                ArrayList<Challenge> result = new ArrayList<Challenge>();
                String[] challengeParts = AUTHENTICATION_HEADER_VALUE_SPLIT_PATTERN.split((CharSequence)authenticationHeaderValue);
                String authScheme = null;
                HashMap<String, String> authParams = new HashMap<String, String>();
                int j = challengeParts.length;
                for (int i = 0; i < j; ++i) {
                    String challengePart = challengeParts[i];
                    if (challengePart.isEmpty()) continue;
                    String newAuthScheme = null;
                    String authParam = null;
                    if (AUTH_SCHEME_PATTERN.matcher(challengePart).matches()) {
                        newAuthScheme = challengePart;
                    } else if (AUTH_SCHEME_AND_TOKEN68_PATTERN.matcher(challengePart).matches()) {
                        String[] authSchemeAndToken68 = WHITESPACE_SPLIT_PATTERN.split(challengePart, 2);
                        newAuthScheme = authSchemeAndToken68[0];
                        if (authParams.put(null, authSchemeAndToken68[1]) != null) {
                            throw new AssertionError();
                        }
                    } else if (AUTH_SCHEME_AND_PARAM_PATTERN.matcher(challengePart).matches()) {
                        String[] authSchemeAndParam = WHITESPACE_SPLIT_PATTERN.split(challengePart, 2);
                        newAuthScheme = authSchemeAndParam[0];
                        authParam = authSchemeAndParam[1];
                    } else if (AUTH_PARAM_PATTERN.matcher(challengePart).matches()) {
                        authParam = challengePart;
                    } else {
                        Matcher matcher;
                        Matcher quotedStringAuthParamAtEndMatcher;
                        StringBuilder patternBuilder = new StringBuilder();
                        patternBuilder.append('^').append(Pattern.quote(challengeParts[0]));
                        for (int i2 = 1; i2 < i; ++i2) {
                            patternBuilder.append(AUTHENTICATION_HEADER_VALUE_SPLIT_PATTERN_PART).append(Pattern.quote(challengeParts[i2]));
                        }
                        do {
                            patternBuilder.append(AUTHENTICATION_HEADER_VALUE_SPLIT_PATTERN_PART).append(Pattern.quote(challengeParts[i++]));
                            matcher = Pattern.compile(patternBuilder.toString()).matcher((CharSequence)authenticationHeaderValue);
                            if (!matcher.find()) {
                                throw new AssertionError();
                            }
                        } while (!(quotedStringAuthParamAtEndMatcher = QUOTED_STRING_AUTH_PARAM_AT_END_PATTERN.matcher(matcher.group())).find());
                        authParam = quotedStringAuthParamAtEndMatcher.group();
                    }
                    if (newAuthScheme != null) {
                        if (authScheme != null) {
                            if (authenticationScheme == null || authScheme.equalsIgnoreCase(authenticationScheme)) {
                                result.add(new Challenge(authScheme, authParams));
                            }
                            authParams.clear();
                        }
                        authScheme = newAuthScheme;
                    }
                    if (authParam == null) continue;
                    String[] authParamPair = AUTH_PARAM_SPLIT_PATTERN.split(authParam, 2);
                    String authParamKey = authParamPair[0].toLowerCase(Locale.US);
                    String authParamValue = authParamPair[1];
                    if (!TOKEN_PATTERN.matcher(authParamValue).matches()) {
                        authParamValue = authParamValue.substring(1, authParamValue.length() - 1);
                        authParamValue = QUOTED_PAIR_PATTERN.matcher(authParamValue).replaceAll("$1");
                    }
                    if (authParams.put(authParamKey, authParamValue) == null) continue;
                    return Collections.emptyList();
                }
                if (authenticationScheme == null || authScheme != null && authScheme.equalsIgnoreCase(authenticationScheme)) {
                    result.add(new Challenge(authScheme, authParams));
                }
                return result;
            }).stream();
        });
    }
}

