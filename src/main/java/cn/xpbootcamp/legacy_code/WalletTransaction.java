package cn.xpbootcamp.legacy_code;

import cn.xpbootcamp.legacy_code.enums.STATUS;
import cn.xpbootcamp.legacy_code.service.WalletService;
import cn.xpbootcamp.legacy_code.utils.IdGenerator;
import cn.xpbootcamp.legacy_code.utils.RedisDistributedLock;
import javax.transaction.InvalidTransactionException;

public class WalletTransaction {

    private String id;
    private Long buyerId;
    private Long sellerId;
    private Long productId;
    private String orderId;
    private Long createdTimestamp;
    private Double amount;
    private STATUS status;
    private String walletTransactionId;
    private RedisDistributedLock redisDistributedLock;
    private WalletService walletService;

    public static int TIMESTAMP_20_DAYS = 1728000000;
    public String INVALID_TRANSACTION_EXCEPTION_MESSAGE = "This is an invalid transaction";

    public WalletTransaction(String preAssignedId, Long buyerId, Long sellerId, Long productId,
        String orderId, Long createdTimestamp, Double amount,
        RedisDistributedLock redisDistributedLock,
        WalletService walletService) {
        this.createdTimestamp = createdTimestamp;
        this.amount = amount;
        this.redisDistributedLock = redisDistributedLock;
        this.walletService = walletService;
        generateTransactionId(preAssignedId);
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.productId = productId;
        this.orderId = orderId;
        this.status = STATUS.TO_BE_EXECUTED;
    }

    private void generateTransactionId(String preAssignedId) {
        if (preAssignedId != null && !preAssignedId.isEmpty()) {
            this.id = preAssignedId;
        } else {
            this.id = IdGenerator.generateTransactionId();
        }
        if (!this.id.startsWith("t_")) {
            this.id = "t_" + preAssignedId;
        }
    }

    public boolean execute() throws InvalidTransactionException {
        if (isInvalidTransaction()) {
            throw new InvalidTransactionException(INVALID_TRANSACTION_EXCEPTION_MESSAGE);
        }
        boolean isLocked = false;
        try {
            isLocked = redisDistributedLock.lock(id);

            if (transactionIsExecuted()) {
                return true;
            }
            if (!isLocked || executionIsOver20Days()) {
                return false;
            }
            String walletTransactionId = walletService.moveMoney(id, buyerId, sellerId, amount);
            if (walletTransactionId != null) {
                this.walletTransactionId = walletTransactionId;
                this.status = STATUS.EXECUTED;
                return true;
            } else {
                this.status = STATUS.FAILED;
                return false;
            }
        } finally {
            if (isLocked) {
                redisDistributedLock.unlock(id);
            }
        }
    }

    private boolean transactionIsExecuted() {
        return status == STATUS.EXECUTED;
    }

    private boolean executionIsOver20Days() {
        return System.currentTimeMillis() - createdTimestamp > TIMESTAMP_20_DAYS;
    }

    private boolean isInvalidTransaction() {
        return buyerId == null || (sellerId == null || amount < 0.0);
    }

}