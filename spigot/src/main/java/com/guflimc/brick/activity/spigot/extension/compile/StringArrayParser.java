package com.guflimc.brick.activity.spigot.extension.compile;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringArrayParser {

    private final static Pattern QUOTED = Pattern.compile("^([\"'])((?:\\\\\\1|(?:(?!\\1)).)*)(\\1)");

    private String buffer;
    private final ParseMode[] modes;

    public StringArrayParser(@NotNull String input, @NotNull ParseMode... modes) {
        this.buffer = input;
        this.modes = modes;
    }

    public StringArrayParser(@NotNull String input, int length) {
        this.buffer = input;
        this.modes = new ParseMode[length];
        for (int i = 0; i < length; i++) {
            this.modes[i] = ParseMode.STRING;
        }
    }

    //

    public String[] parse() {
        List<String> result = new ArrayList<>();
        int i = 0;
        do {
            if (i == modes.length) {
                throw new IllegalArgumentException("Too many arguments given.");
            }

            if (modes[i] == ParseMode.STRING) {
                result.add(nextString());
            } else if (modes[i] == ParseMode.GREEDY) {
                result.add(nextGreedy());
            }
        } while (!buffer.isEmpty());

        if (i < modes.length - 1) {
            throw new IllegalArgumentException("Not enough arguments given.");
        }

        return result.toArray(String[]::new);
    }

    //

    private String nextWord() {
        int index = buffer.indexOf(" ");

        // return until end
        if (index == -1) {
            String result = buffer;
            skip(result.length());
            return result;
        }

        // return until space
        String result = buffer.substring(0, index);
        skip(index + 1); // also skip space
        return result;
    }

    private String nextString() {
        // first try quotes
        Matcher mr = QUOTED.matcher(buffer);
        if (mr.find()) {
            if (mr.end() != buffer.length() && buffer.charAt(mr.end()) != ' ')
                throw new IllegalArgumentException("The given data is malformed.");

            skip(mr.end() + 1); // also skip space
            return mr.group(2);
        }

        return nextWord();
    }

    private String nextGreedy() {
        String result = buffer;
        skip(buffer.length());
        return result;
    }

    private void skip(int amount) {
        if (amount >= buffer.length()) {
            buffer = "";
        } else {
            buffer = buffer.substring(amount);
        }
    }

    public enum ParseMode {
        STRING,
        GREEDY
    }

}
