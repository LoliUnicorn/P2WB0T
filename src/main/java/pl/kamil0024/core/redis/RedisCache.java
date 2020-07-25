package pl.kamil0024.core.redis;

import com.google.common.reflect.TypeToken;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class RedisCache<V> implements Cache<V> {
    private final RedisManager rcm;
    private final int expiry;
    private TypeToken<V> holds;

    public RedisCache(RedisManager rcm, TypeToken<V> holds, int expiry) {
        this.rcm = rcm;
        this.holds = holds;
        this.expiry = expiry;
    }

    @Override
    public V getIfPresent(@Nonnull Object key) {
        return rcm.get(key.toString(), holds);
    }

    @Override
    public V get(@Nonnull String key, @Nonnull Function<? super String, ? extends V> mappingFunction) {
        return rcm.get(key, holds, mappingFunction, expiry);
    }

    @Override
    public Map<String, V> getAllPresent(@Nonnull Iterable<?> keys) {
        Map<String, V> map = new LinkedHashMap<>();
        for (Object obj : keys) {
            String str = obj.toString();
            V v = rcm.get(str, holds);
            if (v != null) map.put(str, v);
        }
        return map;
    }

    public Map<String, V> getAllPresentRaw(@Nonnull Iterable<?> keys) {
        Map<String, V> map = new LinkedHashMap<>();
        for (Object obj : keys) {
            String str = obj.toString();
            V v = rcm.getRaw(str, holds);
            if (v != null) map.put(str, v);
        }
        return map;
    }

    @Override
    public void put(@Nonnull String key, @Nonnull V value) {
        rcm.put(key, holds, value, expiry);
    }

    @Override
    public void putAll(@Nonnull Map<? extends String, ? extends V> map) {
        rcm.putAll(holds, map, expiry);
    }

    @Override
    public long getTTL(@Nonnull Object key) {
        return rcm.ttl(key, holds);
    }

    @Override
    public void invalidateAll() {
        invalidateAllRaw(rcm.scanAll(holds));
    }

    @Override
    public void invalidate(@Nonnull Object key) {
        rcm.invalidate(key, holds);
    }

    @Override
    public void invalidateAll(@Nonnull Iterable<?> keys) {
        rcm.invalidateAll(keys, holds);
    }

    private void invalidateAllRaw(@Nonnull Iterable<?> dbKeys) {
        rcm.invalidateAllRaw(dbKeys);
    }

    @Override
    public Map<String, V> asMap() {
        return getAllPresentRaw(rcm.scanAll(holds));
    }
}
