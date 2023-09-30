package cz.coffee.skjson.api.http;

import com.google.gson.JsonParser;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Copyright coffeeRequired nd contributors
 * <p>
 * Created: sobota (30.09.2023)
 */
public class Test {
    public static void main(String[] args) {
        try (var client = new RequestClient("https://webhook.site/4e2e350b-4a8f-4863-85c5-e833e4ec110b")) {
            client
                    //.post()
                    .get()
                    .test();
                    //.addHeaders(new WeakHashMap<>(Map.of(
//                            "Accept-Content", "application/json",
//                            "Custom-Headers", "Something"
//                    )))
                    //.setContent(JsonParser.parseString("{'Test': 'false'}"))
                    //.addAttachment("C:\\Users\\nexti\\Desktop\\Project-SkJson\\environments\\2.9\\plugins\\bStats\\config.yml")
                    //.postAttachments("{\"Hello\": \"From\"}")
                    //.request();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
