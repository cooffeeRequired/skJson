package cz.coffee.jsonHandler;

import java.io.File;

@SuppressWarnings("unused")

public class Test{

    public static void main(String[] args) {
        String test = "tests/json.json";
        GsonHandler json = new GsonHandler(new File(test));

        json.makeJsonFile();
        json.writeJsonFile("{\"Hello\": true}");

        //json.removeJsonFile();

    }
}
