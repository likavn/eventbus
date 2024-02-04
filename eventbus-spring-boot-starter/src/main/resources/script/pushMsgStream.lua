---
--- 定时推送消息到redis delay stream
--- Created by likavn
--- DateTime: 2024/1/5 09:58
---
--- delay zset key
local delayKey = KEYS[1]
local delayStreamKey = KEYS[2]
--- 当前时间搓
local currentTimeMillis = ARGV[1]
--- 最大推送消息数,默认10万数据
local maxPushCount = ARGV[2] or 1000000
--- 每次处理的最大消息数
local msgCount = 1000

--- 轮询次数
local pollCount = maxPushCount / msgCount
local remainingPushCount = maxPushCount % msgCount

--- 循环推送消息
while pollCount >= 0 do
    if pollCount == 0 then
        if remainingPushCount > 0 then
            msgCount = remainingPushCount
        else
            break
        end
    end
    local expiredValues = redis.call('zrangebyscore', delayKey, 0, currentTimeMillis, 'limit', 0, msgCount)
    if #expiredValues > 0 then
        for _, v in ipairs(expiredValues) do
            redis.call('xadd', delayStreamKey, '*', 'payload', v)
            redis.call('zrem', delayKey, v)
        end
    else
        break
    end
    pollCount = pollCount - 1
end

--- 下个消息的过期时间
local v = redis.call('zrange', delayKey, 0, 0, 'withscores');
if v[1] ~= nil then
    return tonumber(v[2]);
end
return nil;