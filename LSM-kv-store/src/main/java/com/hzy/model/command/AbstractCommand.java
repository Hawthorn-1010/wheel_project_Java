package com.hzy.model.command;

import com.alibaba.fastjson.JSONObject;

/**
 * User: hzy
 * Date: 2022/5/29
 * Time: 17:35
 * Description:
 */
public abstract class AbstractCommand implements Command{
    private CommandTypeEnum type;

    public AbstractCommand(CommandTypeEnum type) {
        this.type = type;
    }

    public String toString() {
        return JSONObject.toJSONString(this);
    }

}
