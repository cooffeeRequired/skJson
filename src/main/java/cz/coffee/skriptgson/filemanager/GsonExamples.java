package cz.coffee.skriptgson.filemanager;

import cz.coffee.skriptgson.utils.GsonErrorLogger;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static cz.coffee.skriptgson.utils.GsonErrorLogger.sendErrorMessage;

public class GsonExamples {

    public GsonExamples() {

        StorageConfigurator sc = new StorageConfigurator();
        if (sc.value("create-examples").toString().equals("true")) {
            download();
            init();
            sc.setValue("create-examples", false);
        }
    }

    public static void init() {
        String fileZip = "plugins/Skript/scripts/scripts.zip";
        File destDir = new File("plugins/Skript/scripts");

        try {
            byte[] buffer = new byte[1024];
            ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip));
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File newFile = newFile(destDir, zipEntry);
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    // fix for Windows-created archives
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }

                    // write file content
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
                zipEntry = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();
            Files.delete(new File("plugins/Skript/scripts/scripts.zip").toPath());
        } catch (IOException e) {
            sendErrorMessage(e.getMessage(), GsonErrorLogger.ErrorLevel.WARNING);
        }
    }

    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    private void download() {
        File file0 = new File("plugins/Skript/scripts/scripts.zip");
        try (BufferedInputStream in = new BufferedInputStream(new URL("https://github.com/cooffeeRequired/skript-gson/raw/main/libs/-skript-gson.zip").openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(file0)) {
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        } catch (IOException ignored) {
        }
    }
}
