/**
 * Copyright 2023-2033, likavn (likavn@163.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.likavn.eventbus.test.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.likavn.eventbus.test.controller.vo.BsConsumerVO;
import com.github.likavn.eventbus.test.entity.BsConsumer;
import org.apache.ibatis.annotations.Param;

/**
 * @author likavn
 * @date 2024/3/31
 **/
public interface BsConsumerMapper extends BaseMapper<BsConsumer> {
    IPage<BsConsumerVO> listPage(@Param("page") IPage<BsConsumerVO> page, @Param("consumer") BsConsumerVO consumer);
}
