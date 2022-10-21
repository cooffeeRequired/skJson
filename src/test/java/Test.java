import cz.coffee.Util.GsonHandler;

import java.io.File;
import java.io.IOException;

public class Test {
    public static void main(String[] args) throws IOException {

        String filename = "Tests/SkriptGson/Test.json";
        String testString =
                """
                {
                    "Hello":
                    true
                }
                """;

        File f = new File("filename");

        boolean c = false;
        if ( !f.exists() ) {
            if (f.createNewFile()) {
                c = true;
            }
        } else {
            c = true;
        }

        if (c) {
            GsonHandler json = new GsonHandler(new File(filename));
            json.makeJsonFile();

            json.writeJsonFile(testString);

            //json.removeJsonFile();

        }

    }
}
