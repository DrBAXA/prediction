package com.vdanyliuk.data;

import java.io.File;

public interface StoredData {
    void load(File file);
    void store(File file);
}
