package cn.xpbootcamp.legacy_code.test_mock_helper;

import cn.xpbootcamp.legacy_code.utils.RedisDistributedLock;

public class MockRedisDistributedLockAlwaysTrue extends RedisDistributedLock {

    @Override
    public boolean lock(String transactionId) {
        return true;
    }

    @Override
    public void unlock(String transactionId) {
    }
}
