---
--- Lua 代码
--- 定时推送消息到redis delay stream
--- Created by likavn
--- DateTime: 2024/1/5 09:58
---
-- 定义全局常量
local MAX_PUSH_COUNT = 100000 -- 最大推送消息数
-- 每次处理的最大消息数
local POLL_INTERVAL = 1000
-- 最大推送消息数
local delayKey = KEYS[1];
local delayStreamKey = KEYS[2];
-- 当前时间搓
local currentTimeMillis = ARGV[1]
-- 假设 ARGV[2] 是每次推送的消息数
local msgCount = ARGV[2] or POLL_INTERVAL

-- 轮询次数
local pollCount = MAX_PUSH_COUNT / msgCount;
local remainingPushCount = MAX_PUSH_COUNT % msgCount;

-- 循环推送消息
while pollCount >= 0 do
    if pollCount == 0 then
        if remainingPushCount > 0 then
            msgCount = remainingPushCount;
        else
            break ;
        end
    end
    local expiredValues = redis.call('ZRANGEBYSCORE', delayKey, 0, currentTimeMillis, 'limit', 0, msgCount);
    if #expiredValues > 0 then
        for _, v in ipairs(expiredValues) do
            redis.call('XADD', delayStreamKey, '*', 'payload', v);
            redis.call('ZREM', delayKey, v);
        end
    else
        break ;
    end
    pollCount = pollCount - 1;
end