package cz.coffee.skjson.utils;

import com.google.gson.JsonElement;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.LinkedList;
import java.util.Queue;

import static cz.coffee.skjson.utils.PatternUtil.keyStruct;

public abstract class JsonParserI {
    public interface Changer {
        void key(LinkedList<PatternUtil.keyStruct> keys, String key);
        void value(LinkedList<PatternUtil.keyStruct> keys, JsonElement value);
    }

    public interface Searcher {
        JsonElement key(Queue<PatternUtil.keyStruct> keys);
    }

    public interface Remover {
        void byValue(LinkedList<keyStruct> keys, JsonElement value);
        void byIndex(LinkedList<keyStruct> keys);
        void byKey(LinkedList<keyStruct> keys);
        void allByValue(LinkedList<keyStruct> keys, JsonElement value);
    }

    public interface Counter {
        int keys(@NonNull String key);
        int values(@NonNull JsonElement value);
    }
}
