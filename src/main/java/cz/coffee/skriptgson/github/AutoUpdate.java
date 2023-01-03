package cz.coffee.skriptgson.github;

import cz.coffee.skriptgson.SkriptGson;
import cz.coffee.skriptgson.filemanager.DefaultConfigFolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AutoUpdate {

    public static final Object dcf = DefaultConfigFolder.readConfigRecords("auto-update");
    private static final File currentJar = new File(SkriptGson.getInstance().getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
    public static boolean updaterStatus = dcf.toString().equals("true");
    private static String currentHash;
    private final String currentVersion = VersionChecker.currentVersion;
    private final String currentVersionTag = VersionHexTagChecker.currentVersionTag;
    private final String gitVersion = VersionChecker.gitHubVersion;
    private final String gitVersionTag = VersionHexTagChecker.gitVersionTag;
    private String gitHash;

    public static void update() {
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

}
