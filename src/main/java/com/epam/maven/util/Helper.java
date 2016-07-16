package com.epam.maven.util;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Vitaliy Boyarsky
 */
public class Helper {

    public static final String SEPARATOR;

    static {
        StringBuilder sb = new StringBuilder(72);
        for (int i = 0; i < 72; i++)
            sb.append('-');
        SEPARATOR = sb.toString();
    }

    private Helper() {
        super();
    }

    public static String replaceAll(Map<Pattern, String> pattern, String original) {
        for (Map.Entry<Pattern, String> entry : pattern.entrySet()) {
            original = entry.getKey().matcher(original).replaceAll(entry.getValue());
        }
        return original;
    }

}
