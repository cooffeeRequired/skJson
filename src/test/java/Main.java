import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import storages.JsonStorage;
import storages.JsonStorage1;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Copyright coffeeRequired nd contributors
 * <p>
 * Created: neděle (03.09.2023)
 */
public class Main {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        JsonStorage<String, JsonElement, File> storage = new JsonStorage<>();
        JsonStorage1<String, JsonElement, File> old_storage = new JsonStorage1<>();

        long startTime = System.nanoTime();
        runTestsStorage(storage, JsonParser.parseString("{}"), new File("test.json"), 1);
        AtomicLong endTime = new AtomicLong(System.nanoTime());
        System.out.println("[SYNC] JsonStorage test trval: " + (endTime.get() - startTime) / 1e6 + " ms");

        long startTime1 = System.nanoTime();
        runTestsStorage1(old_storage, JsonParser.parseString("{}"), new File("test.json"));
        AtomicLong endTime1 = new AtomicLong(System.nanoTime());
        System.out.println("[SYNC] JsonStorage1 test trval: " + (endTime1.get() - startTime1) / 1e6 + " ms");

        startTime = System.nanoTime();
        long finalStartTime = startTime;
        CompletableFuture<Void> asyncTest = CompletableFuture.runAsync(() -> {
            for (int i = 0; i < 50_000_000; i++) {
                runTestsStorage(storage, JsonParser.parseString("{}"), new File("test.json"), i);
            }
        }).whenComplete((completed, throwable) -> {
            endTime.set(System.nanoTime());
            System.out.println("[ASYNC 10_000_00000] JsonStorage test trval: " + (endTime.get() - finalStartTime) / 1e6 + " ms");
        });

        asyncTest.join();

        startTime1 = System.nanoTime();
        long finalStartTime1 = startTime1;
        CompletableFuture<Void> asyncTest1 = CompletableFuture.runAsync(() -> {
            for (int i = 0; i < 50_000_000; i++) {
                try {
                    runTestsStorage1(old_storage, JsonParser.parseString("{}"), new File("test.json"));
                } catch (ExecutionException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).whenComplete((completed, throwable) -> {
            endTime1.set(System.nanoTime());
            System.out.println("[ASYNC 10_000_00000] JsonStorage1 test trval: " + (endTime1.get() - finalStartTime1) / 1e6 + " ms");
        });

        asyncTest1.join();



    }

    private static void runTestsStorage(JsonStorage<String, JsonElement, File> storage, JsonElement json, File file, int i) {
        // Test setValue a getValue
        json.getAsJsonObject().addProperty("value", "innerKey1");

        storage.setValue("key1", json, file);
        assert storage.getValue("key1", json).equals(file);

        // Test removeValue
        storage.removeValue("key1", json);
        assert storage.getValue("key1", json) == null;

        // Test containsKeyAndValue
        assert !storage.containsKeyAndValue("key1", json);
        storage.setValue("key1", json, file);
        assert storage.containsKeyAndValue("key1", json);
    }

    private static void runTestsStorage1(JsonStorage1<String, JsonElement, File> storage1, JsonElement json, File file) throws ExecutionException, InterruptedException {

        json.getAsJsonObject().addProperty("value", "innerKey1");

        // Test addValue a getValue
        storage1.addValue("key1", json, file);
        assert storage1.getValue("key1", json).get().equals(file);

        // Test removeValue
        storage1.removeValue("key1", json);
        assert storage1.getValue("key1", json).get() == null;

        // Test containsKeyAndValue
        assert !storage1.containsKeyAndValue("key1", json);
        storage1.addValue("key1", json, file);
        assert storage1.containsKeyAndValue("key1", json);
    }
}
