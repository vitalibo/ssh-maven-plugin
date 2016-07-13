package com.epam.maven.utils;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Vitaliy Boyarsky
 */
public class Replacer {

    private Replacer() {
        super();
    }

    public static String replaceAll(Map<Pattern, String> pattern, String original) {
        for (Map.Entry<Pattern, String> entry : pattern.entrySet()) {
            original = entry.getKey().matcher(original).replaceAll(entry.getValue());
        }
        return original;
    }

}
