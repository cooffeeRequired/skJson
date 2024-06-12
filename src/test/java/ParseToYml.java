
import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ParseToYml {
    private static final String CLASS_PREFIX = "org.bukkit.";

    public static String parse(File file) {

        List<String> lines = getListFromFileByLines(file);

        for (int i = 0; i < lines.size(); i++) {

            // parse "!" to CLASS_PREFIX + CLASS
            String newLine = Arrays.stream(lines.get(i).split("(?<=\\s)|(?=\\s)"))
                    .map(word -> word.startsWith("!")
                            ? CLASS_PREFIX + capitalizeFirstChar(word.substring(1))
                            : word)
                    .collect(Collectors.joining());

            lines.set(i, newLine);


            String actualLine = lines.get(i);
            String nextLine = i + 1 < lines.size() ? lines.get(i + 1) : null;

            String[] actualLineWords = actualLine.trim().split("\\s+");
            String nextLineFirstWord = nextLine == null ? null : nextLine.trim().split("\\s+")[0];


            if (actualLineWords.length > 1 && actualLineWords[0].endsWith(":") && nextLine != null && nextLineFirstWord.equals("-")) {
                String whitespaces = nextLineWhitespaces(nextLine);

                lines.set(i, actualLineWords[0]);
                lines.add(i + 1, whitespaces + "- " + actualLineWords[1]);
            }
        }

        return String.join("\n", lines);
    }

    private static String nextLineWhitespaces(String nextLine) {
        if (nextLine.startsWith(" ")) {
            int spaceCount = 0;
            while (spaceCount < nextLine.length() && nextLine.charAt(spaceCount) == ' ') {
                spaceCount++;
            }

            return " ".repeat(spaceCount);
        }

        if (nextLine.startsWith("\t")) {
            int tabCount = 0;
            while (tabCount < nextLine.length() && nextLine.charAt(tabCount) == '\t') {
                tabCount++;
            }

            return "\t".repeat(tabCount);
        }
        return "";
    }

    private static String capitalizeFirstChar(String word) {
        return word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
    }

    private static List<String> getListFromFileByLines(File file) {
        Path filePath = Paths.get(file.getAbsolutePath());

        try {
            return Files.readAllLines(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
