package cz.coffee.Util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import lombok.AllArgsConstructor;

import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@AllArgsConstructor
@SuppressWarnings("unused")

// Class is not finished yet.

public class GsonDataApi {

    public static void main(String[] args) throws Exception
    {
        Gson gson = GsonUtils.prettyGson();
        Type DataType = new TypeToken<List<Data>>(){}.getType();
        FileReader fr = new FileReader("Tests/test.json");
        List<Data> dts = gson.fromJson(fr,DataType);
        fr.close();
        if (dts == null) {
            dts = new ArrayList<>();
        }

        // parsing new data
        JsonElement je = GsonUtils.parsedString(
                """
                        {
                            "data": [
                                true,
                                false,
                                null,
                                "A",
                                11,
                                12,
                                22.4,
                                "AAASS",
                                {"Dict": false}
                            ],
                            "AAA": {
                                "Data": false,
                                "BBB": [1,2,3,111.22]
                            }
                        }"""
        );

        // adding
        dts.add(new Data(je, new Date()));
        System.out.println(gson.toJson(dts));

        FileWriter fw = new FileWriter("Tests/test.json");
        gson.toJson(dts, fw);
        fw.close();
    }
}