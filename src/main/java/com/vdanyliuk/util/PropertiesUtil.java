package com.vdanyliuk.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtil {

    public static Properties loadProperties(String fileName) {
        try (InputStream stream = PropertiesUtil.class.getResourceAsStream("/" + fileName)) {
            Properties p = new Properties();
            p.load(stream);
            return p;
        } catch (IOException e) {
            throw new RuntimeException("Can't find properties file " + fileName, e);
        }
    }

}
