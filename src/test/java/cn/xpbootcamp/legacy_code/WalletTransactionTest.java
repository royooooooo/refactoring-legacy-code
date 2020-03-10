package cn.xpbootcamp.legacy_code;

import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.transaction.InvalidTransactionException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class WalletTransactionTest {

    @BeforeAll
    static void beforeAll() {

    }

    @Test
    void shouldThrowExceptionWhenBuyerIdIsNull() {
        WalletTransaction walletTransaction = new WalletTransaction("preAssignedId", null,
            2L, 3L, "orderId", 1.0);
        assertThrows(InvalidTransactionException.class, walletTransaction::execute);
    }

    @Test
    void shouldThrowExceptionWhenSellerIdIsNull() {
        WalletTransaction walletTransaction = new WalletTransaction("preAssignedId", 1L,
            null, 3L, "orderId", 1.0);
        assertThrows(InvalidTransactionException.class, walletTransaction::execute);
    }

    @Test
    void shouldThrowExceptionWhenAmountLessThan0() {
        WalletTransaction walletTransaction = new WalletTransaction("preAssignedId", 1L,
            2L, 3L, "orderId", -0.5);
        assertThrows(InvalidTransactionException.class, walletTransaction::execute);
    }
}