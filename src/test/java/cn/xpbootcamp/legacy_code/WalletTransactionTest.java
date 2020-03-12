package cn.xpbootcamp.legacy_code;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cn.xpbootcamp.legacy_code.service.WalletService;
import cn.xpbootcamp.legacy_code.service.WalletServiceImpl;
import cn.xpbootcamp.legacy_code.utils.RedisDistributedLock;
import javax.transaction.InvalidTransactionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WalletTransactionTest {

    RedisDistributedLock redisDistributedLock = mock(RedisDistributedLock.class);
    Long createdTimestamp;
    WalletService walletService;

    @BeforeEach
    void setUp() {
        createdTimestamp = System.currentTimeMillis();
        walletService = new WalletServiceImpl();
        when(redisDistributedLock.lock(anyString())).thenReturn(true);
    }

    @Test
    void shouldThrowExceptionWhenBuyerIdIsNull() {
        WalletTransaction walletTransaction = new WalletTransaction("preAssignedId", null,
            2L, 3L, "orderId", createdTimestamp, 1.0, redisDistributedLock, walletService);

        assertThrows(InvalidTransactionException.class, walletTransaction::execute);
    }

    @Test
    void shouldThrowExceptionWhenSellerIdIsNull() {
        WalletTransaction walletTransaction = new WalletTransaction("preAssignedId", 1L,
            null, 3L, "orderId", createdTimestamp, 1.0, redisDistributedLock, walletService);

        assertThrows(InvalidTransactionException.class, walletTransaction::execute);
    }

    @Test
    void shouldThrowExceptionWhenAmountLessThan0() {
        WalletTransaction walletTransaction = new WalletTransaction("preAssignedId", 1L,
            2L, 3L, "orderId", createdTimestamp, -0.5, redisDistributedLock, walletService);

        assertThrows(InvalidTransactionException.class, walletTransaction::execute);
    }

    @Test
    void shouldReturnFalseWhenLockFailed() throws InvalidTransactionException {
        when(redisDistributedLock.lock(anyString())).thenReturn(false);
        WalletTransaction walletTransaction = new WalletTransaction("preAssignedId", 1L,
            2L, 3L, "orderId", createdTimestamp, 1.0, redisDistributedLock, walletService);

        assertFalse(walletTransaction.execute());
    }

    @Test
    void shouldReturnFalseWhenExecutionOver20Days() throws InvalidTransactionException {
        createdTimestamp = System.currentTimeMillis() - 1728000001;
        WalletTransaction walletTransaction = new WalletTransaction("preAssignedId", 1L,
            2L, 3L, "orderId", createdTimestamp, 1.0, redisDistributedLock, walletService);

        assertFalse(walletTransaction.execute());
    }

    @Test
    void shouldReturnFalseWhenGetWalletTransactionIdFromServiceIsNull()
        throws InvalidTransactionException {
        walletService = mock(WalletService.class);
        when(walletService.moveMoney(anyString(), anyLong(), anyLong(), anyDouble()))
            .thenReturn(null);
        WalletTransaction walletTransaction = new WalletTransaction("preAssignedId", 1L,
            2L, 3L, "orderId", createdTimestamp, 1.0, redisDistributedLock, walletService);

        assertFalse(walletTransaction.execute());
    }

    @Test
    void shouldReturnTrueWhenGetWalletTransactionIdFromServiceIsNotNull()
        throws InvalidTransactionException {
        walletService = mock(WalletService.class);
        when(walletService.moveMoney(anyString(), anyLong(), anyLong(), anyDouble()))
            .thenReturn("transactionId");
        WalletTransaction walletTransaction = new WalletTransaction("preAssignedId", 1L,
            2L, 3L, "orderId", createdTimestamp, 1.0, redisDistributedLock, walletService);

        assertTrue(walletTransaction.execute());
    }
}