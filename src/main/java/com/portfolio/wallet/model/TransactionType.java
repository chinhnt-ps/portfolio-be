package com.portfolio.wallet.model;

/**
 * Transaction type enum
 */
public enum TransactionType {
    EXPENSE,               // Chi tiêu
    INCOME,                // Thu nhập
    TRANSFER,              // Chuyển khoản nội bộ
    RECEIVABLE_SETTLEMENT, // Nhận tiền cho khoản cho vay (Receivable)
    LIABILITY_SETTLEMENT   // Trả nợ cho khoản nợ (Liability)
}
