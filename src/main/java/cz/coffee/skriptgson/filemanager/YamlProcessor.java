package cz.coffee.skriptgson.filemanager;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.nio.file.Path;

public class YamlProcessor
{

    private static Yaml getConfiguredYaml() {
        DumperOptions options = new DumperOptions();
        options.setIndent(4);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setAllowUnicode(true);
        return new Yaml(options);
    }

    private final File file;
    public YamlProcessor(File file) {
        this.file = file;
    }
    public YamlProcessor(Path path) {
        this.file = path.toFile();
    }
    public YamlProcessor(String fileString) {
        this.file = new File(fileString);
    }

    public Yaml process() {
        return getConfiguredYaml();
    }



}
