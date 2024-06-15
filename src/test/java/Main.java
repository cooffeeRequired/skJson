import cz.coffee.skjson.parser.StringJsonParser;
import cz.coffee.skjson.utils.ConsoleColors;
import cz.coffee.skjson.utils.TimerWrapper;

import java.util.function.Function;

public class Main {
    public static void main(String[] args) {
        Function<String, Void> printOutput = (string) -> {
            System.out.printf("OUTPUT => %s%s%s\n", ConsoleColors.YELLOW, string, ConsoleColors.RESET);
            return null;
        };


        try (var timer = new TimerWrapper(0)) {
            String input = "{userId: 1, products: [{id: 1, quantity: 1}, {id: 50, quantity: 2}]}";
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