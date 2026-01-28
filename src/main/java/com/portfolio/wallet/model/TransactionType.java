package com.portfolio.wallet.model;

/**
 * Transaction type enum
 */
public enum TransactionType {
    EXPENSE,               // Chi tiêu
    INCOME,                // Thu nhập
    TRANSFER,              // Chuyển khoản nội bộ
    RECEIVABLE_SETTLEMENT, // Nhận tiền cho khoản cho vay (Receivable)
    LIABILITY_SETTLEMENT,  // Trả nợ cho khoản nợ (Liability)
    /**
     * Giao dịch điều chỉnh số dư để khớp với số dư thực tế.
     * Về bản chất là một EXPENSE/INCOME kỹ thuật, nhưng tách type riêng để dễ audit.
     */
    BALANCE_ADJUSTMENT
}
