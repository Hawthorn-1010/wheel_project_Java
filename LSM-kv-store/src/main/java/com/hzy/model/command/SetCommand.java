package com.hzy.model.command;

import lombok.Getter;
import lombok.Setter;

/**
 * User: hzy
 * Date: 2022/5/29
 * Time: 17:34
 * Description:
 */
@Getter
@Setter
public class SetCommand extends AbstractCommand{

    private String key;

    private String value;

    public SetCommand(String key, String value) {
        super(CommandTypeEnum.SET);
        this.key = key;
        this.value = value;
    }
}
