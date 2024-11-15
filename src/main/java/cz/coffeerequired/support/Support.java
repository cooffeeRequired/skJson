package cz.coffeerequired.support;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public abstract class Support {
    public static <V> V await(CompletableFuture<V> future, boolean inRun) throws ExecutionException, InterruptedException {
        return inRun ? future.join() : future.get();
    }
}
