package cz.coffeerequired.api.cache;

import java.util.Optional;

public record CacheLink<J, F>(J jsonElement, F file) {
    public Optional<J> getJsonElement() {
        return Optional.ofNullable(jsonElement);
    }
    public Optional<F> getFile() {
        return Optional.ofNullable(file);
    }
}