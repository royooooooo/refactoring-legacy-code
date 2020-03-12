package cn.xpbootcamp.legacy_code;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cn.xpbootcamp.legacy_code.utils.RedisDistributedLock;
import javax.transaction.InvalidTransactionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WalletTransactionTest {

    RedisDistributedLock redisDistributedLock;

    @BeforeEach
    void setUp() {
        redisDistributedLock = RedisDistributedLock.getSingletonInstance();
    }

    @Test
    void shouldThrowExceptionWhenBuyerIdIsNull() {
        WalletTransaction walletTransaction = new WalletTransaction("preAssignedId", null,
            2L, 3L, "orderId", 1.0, redisDistributedLock);
        assertThrows(InvalidTransactionException.class, walletTransaction::execute);
    }

    @Test
    void shouldThrowExceptionWhenSellerIdIsNull() {
        WalletTransaction walletTransaction = new WalletTransaction("preAssignedId", 1L,
            null, 3L, "orderId", 1.0, redisDistributedLock);
        assertThrows(InvalidTransactionException.class, walletTransaction::execute);
    }

    @Test
    void shouldThrowExceptionWhenAmountLessThan0() {
        WalletTransaction walletTransaction = new WalletTransaction("preAssignedId", 1L,
            2L, 3L, "orderId", -0.5, redisDistributedLock);
        assertThrows(InvalidTransactionException.class, walletTransaction::execute);
    }

    @Test
    void shouldReturnFalseWhenLockFailed() throws InvalidTransactionException {
        redisDistributedLock = new MockRedisDistributedLock();
        WalletTransaction walletTransaction = new WalletTransaction("preAssignedId", 1L,
            2L, 3L, "orderId", 1.0, redisDistributedLock);
        assertFalse(walletTransaction.execute());
    }

    static class MockRedisDistributedLock extends RedisDistributedLock {

        @Override
        public boolean lock(String transactionId) {
            return false;
        }
    }
}