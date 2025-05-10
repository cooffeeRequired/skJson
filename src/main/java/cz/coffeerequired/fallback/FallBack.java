package cz.coffeerequired.fallback;

import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.Api;
import cz.coffeerequired.api.Extensible;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public class FallBack {
    private final FallbackProperties fallbackProperties;
    private final File dataFolder = SkJson.getInstance().getDataFolder();
    

    public FallBack() {
        this.fallbackProperties = FallbackProperties.getInstance();
        this.fallback();
    }

    private void containsFallback(File jarFile) throws Exception {
        URL jarURL = jarFile.toURI().toURL();
        // 1. Načti JAR pomocí URLClassLoader
        try (URLClassLoader classLoader = new URLClassLoader(new URL[]{jarURL}, SkJson.class.getClassLoader());
             JarFile jar = new JarFile(jarFile)) {

            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();

                if (name.endsWith(".class") && !entry.isDirectory()) {
                    String className = name.replace('/', '.').replace(".class", "");
                    try {
                        Class<?> cls = classLoader.loadClass(className);
                        if (Extensible.class.isAssignableFrom(cls)) {
                            SkJson.getRegister().registerModule(cls.asSubclass(Extensible.class));
                        }
                    } catch (Throwable t) {
                        SkJson.exception(t, "Failed to load class: %s", className);
                    }
                }
            }
        }
        
    }

    private String getFallbackVersion(File file) {
        String fileName = file.getName();

        int indexOfSkJson = fileName.indexOf("skjson");
        int indexOfFallback = fileName.indexOf("fallback");
        return fileName.substring(indexOfSkJson + "skjson".length() + 1, indexOfFallback - 1);
    }

    private void fallback() {
        if (!Api.Records.PLUGIN_FALLBACK_ENABLED) return;

        var f = new File(dataFolder, "libraries");

        if (f.isDirectory()) {
            var path = f.toPath();
            try (Stream<Path> stream = Files.list(path)) {
                stream
                    .filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".fallback"))
                    .map(Path::toFile)
                    .forEach(t -> {
                        try {
                            SkJson.info("Loading fallback %s", t);

                            String version = getFallbackVersion(t);
                            String propertiesVersion = fallbackProperties.getProperty("version.minimum.supported");

                            if (!version.equals(propertiesVersion)) {
                                SkJson.info("Fallback version %s is not supported, skipping", version);
                                return;
                            }
                            containsFallback(t);
                        } catch (Exception e) {
                            SkJson.exception(e, "Unable to load fallback %s", t);
                        }
                    });
            } catch (IOException e) {
                SkJson.exception(e, "Unable to list files in %s", path);
            }
        }
        

        SkJson.info("Fallback properties: %s", fallbackProperties.getProperty("version.minimum.supported"));
    }

    public boolean isEnabled() {
        return Api.Records.PLUGIN_FALLBACK_ENABLED;
    }
}
