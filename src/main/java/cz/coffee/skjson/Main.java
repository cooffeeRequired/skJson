package cz.coffee.skjson;

import cz.coffee.skjson.parser.StringJsonParser;
import cz.coffee.skjson.utils.TimerWrapper;

public class Main {
    public static void main(String[] args) {
        try (var timer = new TimerWrapper(0)) {
            String input = "{userId: 1, products: [{id: 1, quantity: 1}, {id: 50, quantity: 2}]}";
            var output = StringJsonParser.parseInput(input);


            System.out.println(output);
            System.out.printf("Parsing takes: %s", timer.toHumanTime());
        }
    }
}