import cz.coffee.jsonHandler.JsonHandler;

import java.io.File;

public class Test {
    public static void main(String[] args)  {
        String test = "tests/json.json";
        JsonHandler json = new JsonHandler(new File(test));
        json.makeJsonFile();

        json.writeJsonFile("{\"Hello\": true}");

        json.removeJsonFile();
    }
}
