package com.github.likavn.eventbus.demo.domain;

import com.github.likavn.eventbus.core.metadata.data.MsgBody;
import com.github.likavn.eventbus.demo.constant.MsgConstant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author likavn
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestBody implements MsgBody {
    private String content;

    @Override
    public String code() {
        return MsgConstant.MSG_LISTENER;
    }
}
