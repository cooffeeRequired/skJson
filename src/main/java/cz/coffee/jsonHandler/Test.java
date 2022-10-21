package cz.coffee.jsonHandler;

import java.io.File;

@SuppressWarnings("unused")

public class Test{

    public static void main(String[] args) throws InterruptedException {
        String test = "tests/json.json";
        JsonHandler json = new JsonHandler(new File(test));

        json.makeJsonFile();
        json.writeJsonFile("{\"Hello\": true}");

        //json.removeJsonFile();

    }
}
