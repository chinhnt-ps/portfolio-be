package com.portfolio.wallet.model;

/**
 * Receivable status enum
 */
public enum ReceivableStatus {
    OPEN,              // Mới tạo, chưa thanh toán
    PARTIALLY_PAID,    // Đã thanh toán một phần
    PAID,              // Đã thanh toán đủ
    OVERDUE            // Quá hạn thanh toán
}
