package com.vdanyliuk.data;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
public abstract class Cache<K, V> implements Serializable{

    Map<K, V> cache;

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

    public static <T extends Cache> T load(@NonNull String fileName, Class<? extends T> cacheClass){
        try (InputStream inputStream = new FileInputStream(fileName);
             ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {

            return (T) objectInputStream.readObject();

        } catch (IOException | ClassNotFoundException e) {
            log.error("Can't load cache.");
            try {
                Constructor<? extends T> constructor = cacheClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                return constructor.newInstance();
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException ignored) {
                throw new RuntimeException(ignored);
            }
        }
    }
}
