package com.vdanyliuk.data;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
public abstract class Cache<K, V> implements DataProvider<V>, Serializable{

    private static final long serialVersionUID = 1L;

    protected Map<K, V> cache;

    public Cache() {
        cache = new HashMap<>();
    }

    public V getData(K key) {
        return Optional.ofNullable(cache.get(key))
                .orElseGet(() -> {
                    V data = getNonCachedData(key);
                    cache.put(key, data);
                    return data;
                });
    }

    protected abstract V getNonCachedData(K key);

    public void store(@NonNull String fileName) {
        try (OutputStream outputStream = new FileOutputStream(fileName)) {
            store(outputStream);
        } catch (IOException e) {
            log.error("Can't save cache.");
        }
    }

    public void store(@NonNull OutputStream outputStream) {
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {
            objectOutputStream.writeObject(this);
        } catch (IOException e) {
            log.error("Can't save cache.");
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Cache> T load(@NonNull String fileName, Class<? extends T> cacheClass, Object... args){
        try (InputStream inputStream = new FileInputStream(fileName);
             ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {

            return (T) objectInputStream.readObject();

        } catch (IOException | ClassNotFoundException e) {
            log.warn("Can't load cache. Data will be loaded from network.");
            try {
                Constructor<? extends T> constructor = (Constructor<? extends T>) Stream.of(cacheClass.getDeclaredConstructors())
                        .filter(c -> c.getParameterCount() == args.length)
                        .findAny()
                        .get();

                constructor.setAccessible(true);
                return constructor.newInstance(args);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException ignored) {
                throw new RuntimeException(ignored);
            }
        }
    }
}
