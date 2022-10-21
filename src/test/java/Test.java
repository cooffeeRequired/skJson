import cz.coffee.jsonHandler.GsonHandler;

import java.io.File;

public class Test {
    public static void main(String[] args)  {

        File f = new File("Tests/gson");

        String test = "tests/json.json";
        GsonHandler json = new GsonHandler(new File(test));
        json.makeJsonFile();

        json.writeJsonFile("{\"Hello\": true}");

        json.removeJsonFile();
    }
}
