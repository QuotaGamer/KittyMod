package me.quota73;

public class TimeFormat {

    public static String secondsToFancy(long totalSeconds) {
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();

        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        sb.append(seconds).append("s");

        return sb.toString().trim();
    }

    public static long fancyToSeconds(String input) {
        long total = 0;

        String[] parts = input.toLowerCase().split("\\s+");

        for (String part : parts) {
            if (part.endsWith("d")) {
                total += Long.parseLong(part.replace("d", "")) * 86400;
            } else if (part.endsWith("h")) {
                total += Long.parseLong(part.replace("h", "")) * 3600;
            } else if (part.endsWith("m")) {
                total += Long.parseLong(part.replace("m", "")) * 60;
            } else if (part.endsWith("s")) {
                total += Long.parseLong(part.replace("s", ""));
            }
        }

        return total;
    }
}