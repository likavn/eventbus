---
--- 分布式锁实现
--- Created by likavn
--- DateTime: 2024/1/5 09:58
---
--- 锁key
local key = KEYS[1]
--- 锁的时间，单位：秒
local secondTime = ARGV[1]
--- 判断是否存在锁
if redis.call('exists', key) > 0 then
    return false
else
    redis.call('set', key, 1)
    redis.call('expire', key, secondTime)
    return true
end