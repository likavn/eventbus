package com.github.likavn.eventbus.demo.domain;

import lombok.Data;

/**
 * @author likavn
 * @date 2023/12/26
 **/
@Data
public class TMsg {
    private String id;
    private String name;
    private String content;
    private String type;
    private String time;
    private String status;
}
