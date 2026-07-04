package cz.coffeerequired.api.http;

import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.Api;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Shared {@link HttpClient} and executor for all SkJson HTTP traffic.
 */
public final class HttpClientProvider {

    private static volatile HttpClient httpClient;
    private static volatile ThreadPoolExecutor executor;

    private HttpClientProvider() {
    }

    public static HttpClient getClient() {
        if (httpClient == null) {
            synchronized (HttpClientProvider.class) {
                if (httpClient == null) {
                    httpClient = HttpClient.newBuilder()
                            .executor(getExecutor())
                            .connectTimeout(Duration.ofSeconds(connectTimeoutSeconds()))
                            .version(HttpClient.Version.HTTP_1_1)
                            .followRedirects(HttpClient.Redirect.NORMAL)
                            .build();
                    SkJson.debug("Shared HTTP client initialized (connect timeout %ss)", connectTimeoutSeconds());
                }
            }
        }
        return httpClient;
    }

    public static ThreadPoolExecutor getExecutor() {
        if (executor == null) {
            synchronized (HttpClientProvider.class) {
                if (executor == null) {
                    int threads = Math.max(1, Api.Records.HTTP_MAX_THREADS);
                    executor = new ThreadPoolExecutor(
                            threads,
                            threads * 4,
                            60L,
                            TimeUnit.SECONDS,
                            new LinkedBlockingQueue<>(128),
                            r -> {
                                Thread thread = new Thread(r, "SkJson-HTTP");
                                thread.setDaemon(true);
                                return thread;
                            },
                            (r, pool) -> {
                                SkJson.severe("HTTP queue full (%s active), running task on caller thread", pool.getActiveCount());
                                if (!pool.isShutdown()) {
                                    r.run();
                                }
                            }
                    );
                }
            }
        }
        return executor;
    }

    public static int requestTimeoutSeconds() {
        return Api.Records.HTTP_REQUEST_TIMEOUT_SEC != null
                ? Api.Records.HTTP_REQUEST_TIMEOUT_SEC
                : 30;
    }

    private static int connectTimeoutSeconds() {
        return Api.Records.HTTP_CONNECT_TIMEOUT_SEC != null
                ? Api.Records.HTTP_CONNECT_TIMEOUT_SEC
                : 10;
    }

    public static void shutdown() {
        synchronized (HttpClientProvider.class) {
            if (httpClient != null) {
                httpClient.close();
                httpClient = null;
            }
            if (executor != null) {
                executor.shutdown();
                try {
                    if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                        executor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    executor.shutdownNow();
                    Thread.currentThread().interrupt();
                }
                executor = null;
            }
        }
    }
}
