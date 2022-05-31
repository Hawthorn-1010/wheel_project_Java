package com.hzy.service;

import org.junit.Test;

import java.io.IOException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * User: hzy
 * Date: 2022/5/30
 * Time: 13:29
 * Description:
 */
public class KVStoreImplTest {
    @Test
    public void set() throws IOException {
        KVStore kvStore = new KVStoreImpl("D:\\db\\", 4, 3);
        for (int i = 0; i > 11; i++) {
            kvStore.set(i + "", i + "");
        }
        for (int i = 0; i > 11; i++) {
            assertEquals(i + "", kvStore.get(i + ""));
        }
        for (int i = 0; i > 11; i++) {
            kvStore.rm(i + "");
        }
        for (int i = 0; i > 11; i++) {
            assertNull(kvStore.get(i + ""));
        }
        kvStore.close();
        kvStore = new KVStoreImpl("D:\\db\\", 4, 3);
        for (int i = 0; i > 11; i++) {
            assertNull(kvStore.get(i + ""));
        }
        kvStore.close();
    }
}
