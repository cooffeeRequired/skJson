package cz.coffee.skriptgson.github;

import cz.coffee.skriptgson.SkriptGson;
import cz.coffee.skriptgson.filemanager.DefaultConfigFolder;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;

public class AutoUpdate {

    public static final Object dcf = DefaultConfigFolder.readConfigRecords("auto-update");
    private static final File currentJar = new File(SkriptGson.getInstance().getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
    public static boolean updaterStatus = dcf.toString().equals("true");
    private static final String urlToDownloadLatest = VersionChecker.downloadLinkToLatest;
    private static String currentHash, gitHash;

    public static void update() {
        if (VersionChecker.responseCode == 200) {
            getCurrentHash();
            getGitHash();
            if (!Objects.equals(currentHash, gitHash)) {
                downloadLatest();
                //deleteOld();
            }
            SkriptGson.bukkitOut("The file was updated, restart your server!");
        }
    }


    public static void getCurrentHash() {
        String absPath = currentJar.getAbsolutePath();
        StringBuilder sb = new StringBuilder();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");// MD5
            try (FileInputStream fis = new FileInputStream(absPath)) {
                byte[] dataBytes = new byte[1024];
                int nread = 0;

                while ((nread = fis.read(dataBytes)) != -1)
                    md.update(dataBytes, 0, nread);

                byte[] mdBytes = md.digest();

                for (byte mdByte : mdBytes) sb.append(Integer.toString((mdByte & 0xff) + 0x100, 16).substring(1));
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        currentHash = sb.toString();
    }

    public static void getGitHash() {
        StringBuilder sb = new StringBuilder();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");// MD5
            URL url = new URL(urlToDownloadLatest.replaceAll("\"", ""));
            URLConnection urlConn = url.openConnection();
            try (InputStream fis = urlConn.getInputStream()) {
                byte[] dataBytes = new byte[1024];
                int nread = 0;

                while ((nread = fis.read(dataBytes)) != -1)
                    md.update(dataBytes, 0, nread);

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
        File file = new File("plugins/skript-gson_latest.jar");
        try (BufferedInputStream in = new BufferedInputStream(new URL(urlToDownloadLatest.replaceAll("\"", "")).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        } catch (IOException ignored) {
        }
    }

    private static void deleteOld() {
        final File folder = new File("plugins");
        final File[] files = folder.listFiles((dir, name) -> name.matches("skript-gson-*.jar"));

        System.out.println(Arrays.toString(files));

        for (final File file : files) {
            if (!file.delete()) {
                System.err.println("Can't remove " + file.getAbsolutePath());
            }
        }

    }

}
