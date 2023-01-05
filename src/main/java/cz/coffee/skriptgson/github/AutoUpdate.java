package cz.coffee.skriptgson.github;

import cz.coffee.skriptgson.SkriptGson;
import cz.coffee.skriptgson.filemanager.StorageConfigurator;
import org.bukkit.Bukkit;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import static cz.coffee.skriptgson.utils.GsonErrorLogger.ErrorLevel.WARNING;
import static cz.coffee.skriptgson.utils.GsonErrorLogger.sendErrorMessage;

public class AutoUpdate {

    private static final StorageConfigurator sc = new StorageConfigurator();
    public static final Object dcf = sc.value("auto-update");
    private static final File currentJar = new File(SkriptGson.getInstance().getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
    public static boolean updaterStatus;

    static {
        if (dcf != null)
            updaterStatus = dcf.toString().equals("true");
    }

    private static final String urlToDownloadLatest = VersionChecker.downloadLinkToLatest;
    private static String currentHash, gitHash;

    public static void update() {
        if (VersionChecker.responseCode == 200) {
            getCurrentHash();
            getGitHash();
            if (!Objects.equals(currentHash, gitHash)) {
                downloadLatest();
            }
            SkriptGson.bukkitOut("The file was updated, restarting your server!");
            Bukkit.getScheduler().runTask(SkriptGson.getInstance(), Bukkit::shutdown);
        }
    }


    public static String getCurrentHash(String ...path) {
        String absPath = null;
        if (path.length > 0) {
            if (path[0] != null) {
                absPath = path[0];
            }
        } else {
            absPath = currentJar.getAbsolutePath();
        }

        StringBuilder sb = new StringBuilder();
        try {
            if (absPath == null) return null;
            MessageDigest md = MessageDigest.getInstance("SHA-256");// MD5
            try (FileInputStream fis = new FileInputStream(absPath)) {
                byte[] dataBytes = new byte[1024];
                int read;

                while ((read = fis.read(dataBytes)) != -1)
                    md.update(dataBytes, 0, read);

                byte[] mdBytes = md.digest();

                for (byte mdByte : mdBytes) sb.append(Integer.toString((mdByte & 0xff) + 0x100, 16).substring(1));
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        currentHash = sb.toString();
        return currentHash;
    }

    public static void getGitHash() {
        StringBuilder sb = new StringBuilder();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");// MD5
            URL url = new URL(urlToDownloadLatest.replaceAll("\"", ""));
            URLConnection urlConn = url.openConnection();
            try (InputStream fis = urlConn.getInputStream()) {
                byte[] dataBytes = new byte[1024];
                int read;

                while ((read = fis.read(dataBytes)) != -1)
                    md.update(dataBytes, 0, read);

                byte[] mdBytes = md.digest();

                for (byte mdByte : mdBytes) sb.append(Integer.toString((mdByte & 0xff) + 0x100, 16).substring(1));
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        gitHash = sb.toString();
    }

    private static void downloadLatest() {
        File file0 = new File("plugins/skript-gson.jar");
        try (BufferedInputStream in = new BufferedInputStream(new URL(urlToDownloadLatest.replaceAll("\"", "")).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(file0)) {
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        } catch (IOException ignored) {
        } finally {
            final File folder = new File("plugins");

            final File[] files = folder.listFiles((file, name) -> name.matches("skript-gson.*\\.jar$"));
            File finalFile = null, oldFile = null;
            if (files != null) {
                for (final File file : files) {
                    if (!Objects.equals(getCurrentHash(file.getPath()), gitHash)) {
                        oldFile = file;
                    } else {
                        finalFile = file;
                    }
                }
                try {
                    if (finalFile != null && oldFile != null)
                        Files.copy(finalFile.toPath(), oldFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ioe) {
                    sendErrorMessage(ioe.getMessage(), WARNING);
                }
            }
        }
    }
}
