package com.hzy.service;

import java.io.Closeable;

/**
 * User: hzy
 * Date: 2022/5/29
 * Time: 17:59
 * Description:
 */

public interface KVStore extends Closeable {
    String get(String key);
    void set(String key, String value);
    void rm(String key);
}
