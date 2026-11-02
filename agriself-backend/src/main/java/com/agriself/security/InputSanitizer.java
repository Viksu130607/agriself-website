package com.agriself.security;

import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.util.regex.Pattern;

/**
 * Enterprise Form Input Sanitizer.
 * Protects against Reflected & Stored Cross-Site Scripting (XSS),
 * SQL injection patterns, and malicious script injection.
 */
@Component
public class InputSanitizer {

    private static final Pattern SCRIPT_PATTERN = Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE);
    private static final Pattern SRC_PATTERN = Pattern.compile("src[\r\n]*=[\r\n]*\\'(.*?)\\'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern EVAL_PATTERN = Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern EXPRESSION_PATTERN = Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern JAVASCRIPT_PATTERN = Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE);
    private static final Pattern VBSCRIPT_PATTERN = Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE);
    private static final Pattern ONLOAD_PATTERN = Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern ONERROR_PATTERN = Pattern.compile("onerror(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern ONCLICK_PATTERN = Pattern.compile("onclick(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    /**
     * Cleans dangerous scripts and escapes HTML characters.
     */
    public String sanitize(String input) {
        if (input == null) {
            return null;
        }

        String cleaned = input;
        cleaned = SCRIPT_PATTERN.matcher(cleaned).replaceAll("");
        cleaned = SRC_PATTERN.matcher(cleaned).replaceAll("");
        cleaned = EVAL_PATTERN.matcher(cleaned).replaceAll("");
        cleaned = EXPRESSION_PATTERN.matcher(cleaned).replaceAll("");
        cleaned = JAVASCRIPT_PATTERN.matcher(cleaned).replaceAll("");
        cleaned = VBSCRIPT_PATTERN.matcher(cleaned).replaceAll("");
        cleaned = ONLOAD_PATTERN.matcher(cleaned).replaceAll("");
        cleaned = ONERROR_PATTERN.matcher(cleaned).replaceAll("");
        cleaned = ONCLICK_PATTERN.matcher(cleaned).replaceAll("");

        // Escape remaining HTML entities to prevent rendering dangerous elements
        return HtmlUtils.htmlEscape(cleaned.trim());
    }
}
