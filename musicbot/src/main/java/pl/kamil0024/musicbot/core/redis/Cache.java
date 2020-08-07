package pl.kamil0024.musicbot.core.redis;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.function.Function;

public interface Cache<V> {
    V getIfPresent(@Nonnull Object key);
    V get(@Nonnull String key, @Nonnull Function<? super String, ? extends V> mappingFunction);
    Map<String, V> getAllPresent(@Nonnull Iterable<?> keys);
    void put(@Nonnull String key, @Nonnull V value);
    void putAll(@Nonnull Map<? extends String,? extends V> map);
    void invalidate(@Nonnull Object key);
    long getTTL(@Nonnull Object key);
    void invalidateAll();
    void invalidateAll(@Nonnull Iterable<?> keys);
    Map<String, V> asMap();
}
