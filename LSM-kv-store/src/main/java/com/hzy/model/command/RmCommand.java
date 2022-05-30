package com.hzy.model.command;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * User: hzy
 * Date: 2022/5/29
 * Time: 17:54
 * Description:
 */
@Getter
@Setter
public class RmCommand extends AbstractCommand{
    private String key;

    public RmCommand(String key) {
        super(CommandTypeEnum.RM);
        this.key = key;
    }
}
