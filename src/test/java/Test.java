import cz.coffee.skriptgson.Util.GsonDataApi;

import java.io.File;

public class Test {
    public static void main(String[] args){ //

        GsonDataApi api = new GsonDataApi("Tests/json.json");
        api.createFile();
        File file = api.gsonAppend("");

        System.out.println(file != null);
    }
}