import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import cz.coffee.skjson.parser.StringJsonParser;
import cz.coffee.skjson.utils.ConsoleColors;
import cz.coffee.skjson.utils.TimerWrapper;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.function.Function;

public class Main {
    private static final Function<String, Void> printOutput = (string) -> {
        System.out.printf("OUTPUT => %s%s%s\n", ConsoleColors.YELLOW, string, ConsoleColors.RESET);
        return null;
    };

    public static void main(String[] args) throws URISyntaxException {
        parseInputTest();
        fileHandlerTest();
    }


    private static void fileHandlerTest() throws URISyntaxException {
        try (var timer = new TimerWrapper(0)) {
            ClassLoader classLoader = Main.class.getClassLoader();

            URL testYmlResource = classLoader.getResource("test.yml");
            URL testJsonResource = classLoader.getResource("test.json");

            if (testYmlResource != null && testJsonResource != null) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();

                File testYmlfile = new File(testYmlResource.toURI());
                File testJsonfile = new File(testJsonResource.toURI());

//                JsonElement testJsonResult = FileHandler.get(testYmlfile).join();

                String test = ParseToYml.parse(testYmlfile);

                System.out.println(test);

                Yaml yaml = new Yaml();
                System.out.println("My parser:\n" + yaml.load(test));

//                System.out.println(gson.toJson(testJsonResult));
//                System.out.println(FileHandler.get(testJsonfile).join());
//
//
                System.out.printf("Parsing takes: %s%s%s\n\n", ConsoleColors.GREEN, timer.toHumanTime(), ConsoleColors.RESET);
            }
        }
    }



    private static void parseInputTest() {
        try (var timer = new TimerWrapper(0)) {
            String input = "{userId: 1, products: [{id: 1, quantity: !location}, {id: 50, quantity: 2}]}";
            var output = StringJsonParser.parseInput(input);
            printOutput.apply(output);
            output = StringJsonParser.parseInput("{userid: user's id, products: []}");
            printOutput.apply(output);
            output = StringJsonParser.parseInput("{products: [product of player 1,product of player 2,1,true,\"Ahoj\",{test: \"B\", G: false, G: {W: player's health}}]}");
            printOutput.apply(output);
            output = StringJsonParser.parseInput("[1,true,\"A\",fun(1, false, null, {_A}), player's health, {_hello}, {hard: \"to understood\"}]");
            printOutput.apply(output);
            output = StringJsonParser.parseInput("");
            printOutput.apply(output);
            System.out.printf("Parsing takes: %s%s%s\n\n", ConsoleColors.GREEN, timer.toHumanTime(), ConsoleColors.RESET);
        }
    }
}