package com.hzy.model.sstable;

import lombok.Data;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * User: hzy
 * Date: 2022/5/29
 * Time: 18:13
 * Description: 文件索引部分
 */
@Data
public class TableInfo {
    private long version;
    private long dataStart;
    private long dataLen;
    private long indexStart;
    private long indexLen;
    /**
     * 分段大小
     */
    private long partSize;

    public void writeToFile(RandomAccessFile file) {
        try {
            file.writeLong(partSize);
            file.writeLong(dataStart);
            file.writeLong(dataLen);
            file.writeLong(indexStart);
            file.writeLong(indexLen);
            file.writeLong(version);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 读取，倒着读，每个都占？
     * @param file
     * @return
     */
    public static TableInfo readFromFile(RandomAccessFile file) {
        try {
            TableInfo tableInfo = new TableInfo();
            long fileLength = file.length();

            file.seek(fileLength - 8);
            tableInfo.setVersion(file.readLong());

            file.seek(fileLength - 8 * 2);
            tableInfo.setIndexLen(file.readLong());

            file.seek(fileLength - 8 * 3);
            tableInfo.setIndexStart(file.readLong());

            file.seek(fileLength - 8 * 4);
            tableInfo.setDataLen(file.readLong());

            file.seek(fileLength - 8 * 5);
            tableInfo.setDataStart(file.readLong());

            file.seek(fileLength - 8 * 6);
            tableInfo.setPartSize(file.readLong());

            return tableInfo;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}












