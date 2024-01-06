package cz.coffee.skjson.api.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Copyright coffeeRequired nd contributors
 * <p>
 * Created: sobota (30.09.2023)
 */
public class RequestClientUtils {
    protected static String colorizedMethod(String method) {
        StringBuilder sb = new StringBuilder();
        sb.append("&l");
        switch (method) {
            case "GET" -> sb.append("&aGET");
            case "POST" -> sb.append("&bPOST");
            case "PUT" -> sb.append("&7PUT");
            case "DELETE" -> sb.append("&cDELETE");
            case "HEAD" -> sb.append("&3HEAD");
            case "PATCH" -> sb.append("&ePATCH");
            case "MOCK" -> sb.append("&5MOCK");
        }
        sb.append("&r");
        return sb.toString();
    }

    protected static File changeExtension(File f, String newExtension) throws IOException {
        int i = f.getName().lastIndexOf('.');
        String name = f.getName().substring(0, i);
        File tempFile = File.createTempFile(name + ".sk -- ", newExtension);

        try (FileInputStream fis = new FileInputStream(f);
             FileOutputStream fos = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }

        return tempFile;
    }
}
