---
--- 延时数据发送
--- Created by likavn
--- DateTime: 2024/1/5 09:58
---
--- 延时队列key
local delayKey = KEYS[1]
--- timeout
local timeout = ARGV[1]
--- 延时数据
local jsBody = ARGV[2]
---- 添加数据
redis.call('ZADD', delayKey, timeout, jsBody);

--- 下个消息的过期时间
local v = redis.call('zrange', delayKey, 0, 0, 'withscores');
if v[1] ~= nil then
    return tonumber(v[2]);
end
return nil;