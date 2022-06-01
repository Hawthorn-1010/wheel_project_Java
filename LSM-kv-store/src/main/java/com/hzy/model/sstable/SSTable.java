package com.hzy.model.sstable;


import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.hzy.model.Position;
import com.hzy.model.command.Command;
import com.hzy.model.command.RmCommand;
import com.hzy.model.command.SetCommand;
import com.hzy.utils.ConvertUtil;
import com.hzy.utils.LoggerUtil;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Optional;
import java.util.TreeMap;

/**
 * User: hzy
 * Date: 2022/5/29
 * Time: 18:13
 * Description:
 */
public class SSTable implements Closeable {

    private final String RW = "rw";

    private final Logger LOGGER = LoggerFactory.getLogger(SSTable.class);

    private TableInfo tableInfo;

    /**
     * 稀疏索引
     */
    private TreeMap<String, Position> sparseIndex;

    private final RandomAccessFile tableFile;

    private final String filePath;

    public SSTable(String filePath, int partSize) {
        this.tableInfo = new TableInfo();
        this.tableInfo.setPartSize(partSize);
        this.filePath = filePath;

        try {
            this.tableFile = new RandomAccessFile(filePath, RW);
            tableFile.seek(0);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        sparseIndex = new TreeMap<>();
    }

    public static SSTable createFromIndex(String filePath, int partSize, TreeMap<String, Command> index) {
        SSTable ssTable = new SSTable(filePath, partSize);
        ssTable.initFromIndex(index);
        return ssTable;
    }

    public static SSTable createFromFile(String filePath) {
        SSTable ssTable = new SSTable(filePath, 0);
        ssTable.restoreFromFile();
        return ssTable;
    }

    /**
     * 从SSTable中查询数据
     * @param key
     * @return
     */
    public Command query(String key) {
        try {
            LinkedList<Position> sparseKeyPositionList = new LinkedList<>();
            Position lowerPosition = null;
            Position higherPosition = null;

            // 寻找lowerPosition和higherPosition
            for (String k : sparseIndex.keySet()) {
                if (k.compareTo(key) <= 0) {
                    lowerPosition = sparseIndex.get(k);
                } else {
                    higherPosition = sparseIndex.get(k);
                    break;
                }
            }
            if (lowerPosition != null) {
                sparseKeyPositionList.add(lowerPosition);
            }
            if (higherPosition != null) {
                sparseKeyPositionList.add(higherPosition);
            }
            // 为空
            if (sparseKeyPositionList.size() == 0) {
                return null;
            }
            LoggerUtil.debug(LOGGER, "[SSTable][restoreFromFile][sparseKeyPositionList]: {}", sparseKeyPositionList);
            // ?
            Position firstPosition = sparseKeyPositionList.getFirst();
            // Position lastPosition = sparseKeyPositionList.getLast();
            long start = 0;
            long len = 0;
            start = firstPosition.getStart();
//            if (firstPosition.equals(lastPosition)) {
            len = firstPosition.getLength();
//            } else {
//                // ?
//                len = lastPosition.getStart() + lastPosition.getLength() - start;
//            }

            // key如果存在必定位于区间内，只需读取区间内的数据，减少IO
            byte[] dataPart = new byte[(int) len];
            tableFile.seek(start);
            // 读取区间内
            tableFile.read(dataPart);
            int pStart = 0;
            // 读取分区数据
            for (Position position : sparseKeyPositionList) {
                // 将该稀疏索引的分区读入
                JSONObject dataPartJson = JSONObject.parseObject(new String(dataPart, pStart, (int) position.getLength()));
                LoggerUtil.debug(LOGGER, "[SSTable][restoreFromFile][dataPartJson]: {}", dataPartJson);
                if (dataPartJson.containsKey(key)) {
                    JSONObject value = dataPartJson.getJSONObject(key);
                    return ConvertUtil.jsonToCommand(value);
                }
                pStart += (int) position.getLength();
            }
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 内存表数据转化为SSTable
     * @param index
     */
    private void initFromIndex(TreeMap<String, Command> index) {
        try {
            // 有序的
            JSONObject partData = new JSONObject(true);
            tableInfo.setDataStart(tableFile.getFilePointer());
            // 保存数据区？
            for (Command command : index.values()) {
                // 处理set命令
                if (command instanceof SetCommand) {
                    SetCommand set = (SetCommand) command;
                    partData.put(set.getKey(), set);
                }
                // 处理rm命令
                if (command instanceof RmCommand) {
                    RmCommand rm = (RmCommand) command;
                    partData.put(rm.getKey(), rm);
                }
                // SSTable达到阈值，开始写入数据段
                if (partData.size() >= tableInfo.getPartSize()) {
                    writeDataPart(partData);
                }
            }

            // 遍历完后，尾部数据不一定达到分段条件，需要写入
            if (partData.size() > 0) {
                // 写入数据区
                writeDataPart(partData);
            }

            long dataPartLen = tableFile.getFilePointer() - tableInfo.getDataStart();
            tableInfo.setDataLen(dataPartLen);

            // 保存稀疏索引
            byte[] indexBytes = JSONObject.toJSONString(sparseIndex).getBytes(StandardCharsets.UTF_8);
            tableInfo.setIndexStart(tableFile.getFilePointer());
            // 向文件写入稀疏索引部分
            tableFile.write(indexBytes);
            tableInfo.setIndexLen(indexBytes.length);
            LoggerUtil.debug(LOGGER, "[SSTable][initFromIndex][sparseIndex]: {}", sparseIndex);

            // 保存文件索引
            tableInfo.writeToFile(tableFile);
            LoggerUtil.info(LOGGER, "[SSTable][initFromIndex]: {}, {}", filePath, tableInfo);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 文件中的SSTable恢复到内存
     */
    private void restoreFromFile() {
        try {
            // 读取文件索引
            TableInfo tableInfo = TableInfo.readFromFile(tableFile);
            LoggerUtil.debug(LOGGER, "[SSTable][restoreFromFile][tableInfo]: {}", tableInfo);
            // 读取稀疏索引
            byte[] indexBytes = new byte[(int) tableInfo.getIndexLen()];
            tableFile.seek(tableInfo.getIndexStart());
            tableFile.read(indexBytes);

            // 转格式
            String indexStr = new String(indexBytes, StandardCharsets.UTF_8);
            LoggerUtil.debug(LOGGER, "[SSTable][restoreFromFile][indexStr]: {}", indexStr);
            // JSON格式存储索引,?只存一个
            sparseIndex = JSONObject.parseObject(indexStr,
                    new TypeReference<TreeMap<String, Position>>() {
                    });
            this.tableInfo = tableInfo;
            LoggerUtil.debug(LOGGER, "[SSTable][restoreFromFile][sparseIndex]: {}", sparseIndex);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 数据分区写入文件?
     * @param partData
     */
    private void writeDataPart(JSONObject partData) throws IOException {
        byte[] partDataBytes = partData.toJSONString().getBytes(StandardCharsets.UTF_8);
        // 获取RandomAccessFile流中的当前指针
        long start = tableFile.getFilePointer();
        tableFile.write(partDataBytes);

        // 记录数据段第一个数到稀疏索引
        Optional<String> firstKey = partData.keySet().stream().findFirst();
        // 索引结构 {key, {start, len}}
        firstKey.ifPresent(s -> sparseIndex.put(s, new Position(start, partDataBytes.length)));
        partData.clear();
    }

    @Override
    public void close() throws IOException {
        tableFile.close();
    }
}

















