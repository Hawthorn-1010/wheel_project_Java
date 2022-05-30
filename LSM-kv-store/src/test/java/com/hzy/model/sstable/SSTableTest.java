package com.hzy.model.sstable;

import com.hzy.model.command.Command;
import com.hzy.model.command.RmCommand;
import com.hzy.model.command.SetCommand;
import org.junit.Test;

import java.util.TreeMap;

/**
 * User: hzy
 * Date: 2022/5/30
 * Time: 13:28
 * Description:
 */
public class SSTableTest {

    @Test
    public void testCreateFromIndex() {
        TreeMap<String, Command> index = new TreeMap<>();
        for (int i = 0; i < 10; i++) {
            SetCommand setCommand = new SetCommand("key" + i, "value" + i);
            index.put(setCommand.getKey(), setCommand);
        }
        index.put("key100", new SetCommand("key100", "value100"));
        index.put("key100", new RmCommand("key100"));
        SSTable ssTable = SSTable.createFromIndex("test.txt", 3, index);
    }

    @Test
    public void testCreateFromFile() {
        SSTable ssTable = SSTable.createFromFile("test.txt");
        System.out.println(ssTable.query("key4"));
    }
}
