package com.vdanyliuk.data;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Slf4j
public abstract class Cache implements Serializable{


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
                return cacheClass.newInstance();
            } catch (InstantiationException | IllegalAccessException ignored) {
                return null;
            }
        }
    }
}
