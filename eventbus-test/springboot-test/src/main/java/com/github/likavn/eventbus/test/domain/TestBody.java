package com.github.likavn.eventbus.test.domain;

import com.github.likavn.eventbus.core.metadata.data.MsgBody;
import com.github.likavn.eventbus.test.constant.MsgConstant;
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
        return MsgConstant.DEMO_MSG_LISTENER;
    }
}
