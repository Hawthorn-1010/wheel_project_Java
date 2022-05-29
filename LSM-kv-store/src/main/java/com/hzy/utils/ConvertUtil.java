package com.hzy.utils;

import com.alibaba.fastjson.JSONObject;
import com.hzy.model.command.Command;
import com.hzy.model.command.CommandTypeEnum;
import com.hzy.model.command.RmCommand;
import com.hzy.model.command.SetCommand;

/**
 * User: hzy
 * Date: 2022/5/29
 * Time: 17:26
 * Description:
 */
public class ConvertUtil {
    public static final String TYPE = "type";

    public static Command jsonToCommand(JSONObject value) {
        if (value.getString(TYPE).equals(CommandTypeEnum.SET.name())) {
            return value.toJavaObject(SetCommand.class);
        } else if (value.getString(TYPE).equals(CommandTypeEnum.RM.name())) {
            return value.toJavaObject(RmCommand.class);
        }
        return null;
    }
}
