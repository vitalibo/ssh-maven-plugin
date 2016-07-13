package com.epam.maven.utils;

/**
 * @author Vitaliy Boyarsky
 */
public class Separator {

    public static final String TO_STRING;

    static {
        StringBuilder sb = new StringBuilder(72);
        for (int i = 0; i < 72; i++)
            sb.append('-');
        TO_STRING = sb.toString();
    }

    private Separator() {
        super();
    }

}