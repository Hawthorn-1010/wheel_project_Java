package com.hzy.service;

import com.hzy.model.command.Command;
import com.hzy.model.sstable.SSTable;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * User: hzy
 * Date: 2022/5/29
 * Time: 18:02
 * Description:
 */
public class KVStoreImpl implements KVStore{

    public static final String TABLE = ".table";
    public static final String WAL = "wal";
    public static final String RW_MODE = "rw";
    public static final String WAL_TMP = "walTmp";

    private TreeMap<String, Command> index;

    private TreeMap<String, Command> immutableIndex;

    private final LinkedList<SSTable> ssTables;

    private final String dataDir;

    private final ReadWriteLock indexLock;

    private final int storeThreshold;

    private final int partSize;

    private RandomAccessFile wal;

    private File walFile;

    public KVStoreImpl(String dataDir, int storeThreshold, int partSize) {
        this.dataDir = dataDir;
        this.indexLock = new ReentrantReadWriteLock();
        this.storeThreshold = storeThreshold;
        this.partSize = partSize;

        ssTables = new LinkedList<>();

    }

    @Override
    public String get(String key) {
        return null;
    }

    @Override
    public void set(String key, String value) {

    }

    @Override
    public void rm(String key) {

    }

    private void restoreFromWal(RandomAccessFile wal) {

    }

    private void switchIndex() {

    }

    private void storeToSsTable() {

    }

    @Override
    public void close() throws IOException {

    }
}
