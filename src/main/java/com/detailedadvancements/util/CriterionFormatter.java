package com.detailedadvancements.util;

public class CriterionFormatter {

    public static String format(String criterionId) {
        String name;
        if (criterionId.contains(":")) {
            String[] parts = criterionId.split(":", 2);
            name = parts[1];
        } else {
            name = criterionId;
        }
        name = name.replace('/', ' ').replace('_', ' ');
        return toTitleCase(name);
    }

    private static String toTitleCase(String input) {
        String[] words = input.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(word.charAt(0)));
            if (word.length() > 1) sb.append(word.substring(1).toLowerCase());
        }
        return sb.toString();
    }
}
