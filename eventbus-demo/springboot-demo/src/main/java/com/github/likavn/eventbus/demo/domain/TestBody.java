package com.github.likavn.eventbus.demo.domain;

import com.github.likavn.eventbus.core.metadata.data.AbstractBody;
import com.github.likavn.eventbus.demo.constant.MsgConstant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author likavn
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TestBody extends AbstractBody {
    private String content;

    @Override
    public String code() {
        return MsgConstant.TEST_MSG_SUBSCRIBE_LISTENER;
    }
}
