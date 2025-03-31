package com.carsharing.carsharing.cache;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LRUCache<K, V> {

    private final int maxCapacity;
    private final Map<K, CacheEntry<V>> cache;

    // Внутренний класс для хранения значения и времени последнего доступа
    private static class CacheEntry<V> {
        V value;
        long lastAccessTime;
        int frequency;

        CacheEntry(V value) {
            this.value = value;
            this.lastAccessTime = System.currentTimeMillis(); // Время создания записи
            this.frequency = 1;
        }
    }

    public LRUCache(int maxCapacity) {
        this.maxCapacity = maxCapacity;
        this.cache = new LinkedHashMap<K, CacheEntry<V>>(maxCapacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, CacheEntry<V>> eldest) {
                // Удаляем самый старый элемент, если размер превышает maxCapacity
                boolean shouldRemove = size() > maxCapacity;
                if (shouldRemove) {
                    log.info(
                            "Cache eviction: Removed least recently used item with key {} (frequency: {}))",
                            eldest.getKey(),
                            eldest.getValue().frequency
                    );
                }
                return shouldRemove;
            }
        };
    }

    // Получение значения по ключу
    public synchronized V get(K key) {
        CacheEntry<V> entry = cache.get(key);
        if (entry != null) {
            entry.lastAccessTime = System.currentTimeMillis(); // Обновляем время доступа
            entry.frequency++;
            log.info(
                    "Cache hit: Retrieved item with key {} from cache (frequency: {})",
                    key,
                    entry.frequency
            );
            return entry.value;
        }
        log.info("Cache miss: Item with key {} not found in cache", key);
        return null;
    }

    // Добавление или обновление значения по ключу
    public synchronized void put(K key, V value) {
        if (cache.containsKey(key)) {
            CacheEntry<V> entry = cache.get(key);
            entry.value = value;
            entry.lastAccessTime = System.currentTimeMillis(); // Обновляем время доступа
            entry.frequency++;
            log.info(
                    "Cache update: Updated item with key {} in cache (frequency: {})",
                    key,
                    entry.frequency
            );
        } else {
            cache.put(key, new CacheEntry<>(value));
            log.info("Cache add: Added item with key {} to cache ", key);
        }
    }

    // Удаление значения по ключу
    public synchronized void remove(K key) {
        if (cache.remove(key) != null) {
            log.info("Cache remove: Removed item with key {} from cache", key);
        }
    }

    // Очистка кэша
    public synchronized void clear() {
        cache.clear();
        log.info("Cache cleared: All items removed");
    }

    public synchronized V getOrFetch(K key, Function<K, V> fetchFunc) {
        V value = get(key);
        if (value == null) {
            log.info("Cache miss: Fetching item with key {} from database", key);
            value = fetchFunc.apply(key);
            if (value != null) {
                put(key, value);
            }
        }
        return value;
    }
}