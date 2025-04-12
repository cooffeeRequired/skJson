package cz.coffeerequired.api.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

@SuppressWarnings("unused")
public class RequestClientUtils {

    /**
     * Changes the extension of a file and creates a new temporary file.
     *
     * @param file         The original file.
     * @param newExtension The new file extension (e.g., ".json").
     * @return A new file with the updated extension.
     * @throws IOException If an error occurs during file operations.
     */
    public static File changeExtension(File file, String newExtension) throws IOException {
        String baseName = file.getName().substring(0, file.getName().lastIndexOf('.'));
        File tempFile = File.createTempFile(baseName, newExtension);

        try (FileInputStream fis = new FileInputStream(file);
             FileOutputStream fos = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }

        return tempFile;
    }

    /**
     * Colorizes HTTP methods for better readability in logs.
     *
     * @param method The HTTP method name (e.g., "GET", "POST").
     * @return A colorized version of the method.
     */
    public static String colorizeMethod(String method) {
        return switch (method.toUpperCase()) {
            case "GET" -> "GREEN(GET)";
            case "POST" -> "BLUE(POST)";
            case "PUT" -> "GRAY(PUT)";
            case "DELETE" -> "RED(DELETE)";
            case "HEAD" -> "CYAN(HEAD)";
            case "PATCH" -> "YELLOW(PATCH)";
            default -> "UNKNOWN(" + method + ")";
        };
    }
}
