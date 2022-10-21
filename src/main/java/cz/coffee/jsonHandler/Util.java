package cz.coffee.jsonHandler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;


@SuppressWarnings({"unused", "deprecation"})

public class Util
{
    public static JsonElement gsonParser(String str) { return new JsonParser().parse(str); }
    public static Gson newGson() {return new Gson();}
    public static Gson prettyGson() { return new GsonBuilder().setPrettyPrinting().create();}

    public static String SanitizeString(Object strObj) {
        return String.valueOf(strObj).replaceAll("[\"'][\\w\\s]+[\"']|\\w+[\"']\\w+", "");
    }

    public static JsonElement parsedString(String toParsed) {
        Gson g = prettyGson();
        return gsonParser(toParsed);
    }
}
