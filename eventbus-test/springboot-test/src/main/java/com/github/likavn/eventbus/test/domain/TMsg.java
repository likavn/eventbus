package com.github.likavn.eventbus.test.domain;

import lombok.Data;

/**
 * @author likavn
 * @date 2024/01/01
 **/
@Data
public class TMsg {
    private String id;
    private String name;
    private String content;
    private Integer type;
    private String time;
    private String status;
}
