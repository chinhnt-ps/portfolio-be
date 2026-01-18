package com.portfolio.wallet.model;

/**
 * Settlement type enum
 * Indicates whether settlement is for a Receivable or Liability
 */
public enum SettlementType {
    RECEIVABLE,  // Settlement for a receivable (user receives payment)
    LIABILITY    // Settlement for a liability (user makes payment)
}
