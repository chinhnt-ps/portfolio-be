package com.portfolio.wallet.model;

/**
 * Account type enum
 */
public enum AccountType {
    CASH,       // Tiền mặt
    BANK,       // Ngân hàng
    E_WALLET,   // Ví điện tử
    SAVINGS,    // Tiết kiệm (không tính vào "Tiền hiện có" ở UI)
    INVESTMENT, // Đầu tư (không tính vào "Tiền hiện có" ở UI)
    POSTPAID,   // Trả sau (hiển thị dư nợ thay vì số dư)
    OTHER       // Khác
}
