package com.github.likavn.eventbus.provider.redis;

import com.github.likavn.eventbus.core.exception.EventBusException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * redis lua脚本管理
 *
 * @author likavn
 * @date 2024/1/5
 **/
@Slf4j
@SuppressWarnings("all")
public class RedisLuaManage {
    private static final Map<String, String> SCRIPT_MAP = new ConcurrentHashMap<>(4);

    public RedisLuaManage(String... scriptKeys) {
        for (String scriptKey : scriptKeys) {
            SCRIPT_MAP.put(scriptKey, loadScript(scriptKey));
        }
    }

    /**
     * 获取Lua脚本
     *
     * @param scriptKey 脚本key
     * @return Lua脚本
     */
    public String getScript(String scriptKey) {
        return SCRIPT_MAP.get(scriptKey);
    }

    /**
     * 加载Lua脚本
     *
     * @param scriptKey 脚本key
     * @return Lua脚本
     */
    private String loadScript(String scriptKey) {
        ClassPathResource luaScriptResource = new ClassPathResource("scripts/" + scriptKey);
        try {
            List<String> lines = Files.readAllLines(luaScriptResource.getFile().toPath());
            StringBuilder sb = new StringBuilder();
            lines.forEach(line -> {
                int index = line.indexOf("--");
                if (index > -1) {
                    line = line.substring(0, index);
                }
                line = line.replace("\n", "");
                if (StringUtils.hasLength(line)) {
                    sb.append(line);
                    sb.append("\n");
                }
            });
            log.debug("加载lua脚本{}\n{}", scriptKey, sb.toString());
            return sb.toString();
        } catch (IOException e) {
            throw new EventBusException("Failed to read Lua script file", e);
        }
    }
}