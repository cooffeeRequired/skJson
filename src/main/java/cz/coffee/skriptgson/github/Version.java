package cz.coffee.skriptgson.github;

import cz.coffee.skriptgson.SkriptGson;

public class Version {

    private static final String gitVersion = VersionChecker.gitHubVersion;
    private static final String userVersion = VersionChecker.currentVersion;
    private static final String gitVersionTag = VersionHexTagChecker.gitVersionTag;
    private static final String userVersionTag = VersionHexTagChecker.currentVersionTag;

    private static boolean connectionOut = false;

    public static void check() {
        SkriptGson.bukkitOut("Auto-Updater status " + (AutoUpdate.updaterStatus ? "&aEnabled" : "&cDisabled"));
        if (!(userVersion.equals(gitVersion))) {
            if (gitVersion == null) {
                SkriptGson.bukkitOut("We can't check the version, try check our ethernet connection.");
                connectionOut = true;
            } else {
                SkriptGson.bukkitOut("You're running on outdated version &c " + userVersion + "&r!");
                SkriptGson.bukkitOut("You can download the latest from this web-page &e" + SkriptGson.getInstance().getDescription().getWebsite() + "releases/latest");
                if (AutoUpdate.updaterStatus) {
                    AutoUpdate.update();
                }
            }
        } else {
            if (!connectionOut) {
                SkriptGson.bukkitOut("You're currently running the &alatest&r stable version of skript-gson");
                if (!(userVersionTag.equals(gitVersionTag))) {
                    SkriptGson.bukkitOut("Your version is latest but the github file contains some changes, Check github");
                    SkriptGson.bukkitOut("You can download the latest from this web-page &e" + SkriptGson.getInstance().getDescription().getWebsite() + "releases/latest");
                    if (AutoUpdate.updaterStatus) {
                        AutoUpdate.update();
                    }
                }
            }
        }
    }
}
