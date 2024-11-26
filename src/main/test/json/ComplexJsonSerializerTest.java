package json;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.json.SerializedJson;
import cz.coffeerequired.api.json.SkriptJsonInputParser;
import cz.coffeerequired.support.AnsiColorConverter;

public class ComplexJsonSerializerTest {
    public static void main(String[] args) {

        SerializedJson serialized = new SerializedJson(JsonParser.parseString("{raw: 1, array: [2, false, {nested: true}], object: {key: value}}"));
        var tokenized = SkriptJsonInputParser.tokenize("array::2::nested", "::");

        serialized.changer.key(tokenized, "nested_changed");

        tokenized = SkriptJsonInputParser.tokenize("array::2::nested_changed", "::");

        serialized.changer.value(tokenized, new JsonPrimitive("SUS"));

        tokenized = SkriptJsonInputParser.tokenize("array::1", "::");

        serialized.changer.value(tokenized, new JsonPrimitive("CHANGED"));

        SkJson.logger().info(serialized.toString());


        SerializedJson serialized2 = new SerializedJson(JsonParser.parseString("{raw: 1, array: [2, false, {raw: true}], object: {raw: value}}"));

        System.out.println(AnsiColorConverter.hexToAnsi("#47a5ff") + serialized2.counter.keys("raw") + AnsiColorConverter.RESET);
        System.out.println(AnsiColorConverter.hexToAnsi("#d95ff2") + serialized2.counter.values("value") + AnsiColorConverter.RESET);
        System.out.println(AnsiColorConverter.hexToAnsi("#ffea23") + serialized2.counter.values(2) + AnsiColorConverter.RESET);

        tokenized = SkriptJsonInputParser.tokenize("array::1", "::");

        serialized.remover.byIndex(tokenized);

        System.out.println(AnsiColorConverter.hexToAnsi("#4618ff") + serialized + AnsiColorConverter.RESET);

        tokenized = SkriptJsonInputParser.tokenize("array::1::nested_changed", "::");

        serialized.remover.byKey(tokenized);

        System.out.println(AnsiColorConverter.hexToAnsi("#1cedff") + serialized + AnsiColorConverter.RESET);

        tokenized = SkriptJsonInputParser.tokenize("array::1", "::");

        serialized.remover.byValue(tokenized, new JsonObject());

        System.out.println(AnsiColorConverter.hexToAnsi("#ff0ae2") + serialized + AnsiColorConverter.RESET);

        tokenized = SkriptJsonInputParser.tokenize("raw", "::");

        serialized.remover.byKey(tokenized);

        System.out.println(AnsiColorConverter.hexToAnsi("#ff0ae2") + serialized + AnsiColorConverter.RESET);


        tokenized = SkriptJsonInputParser.tokenize("array::0", "::");

        serialized.remover.byIndex(tokenized);

        System.out.println(AnsiColorConverter.hexToAnsi("#ff0ae2") + serialized + AnsiColorConverter.RESET);


        tokenized = SkriptJsonInputParser.tokenize("object::key", "::");

        var val = serialized.searcher.key(tokenized);

        System.out.println(AnsiColorConverter.hexToAnsi("#16ffb2") + val.getClass() + AnsiColorConverter.RESET);


    }
}
