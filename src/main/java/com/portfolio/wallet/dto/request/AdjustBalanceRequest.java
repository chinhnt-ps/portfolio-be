package com.portfolio.wallet.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for adjusting account balance to match actual balance
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdjustBalanceRequest {

    /**
     * Số dư thực tế hiện tại mà người dùng nhập vào
     */
    @NotNull(message = "Actual balance is required")
    @Min(value = 0, message = "Actual balance must be >= 0")
    private BigDecimal actualBalance;

    /**
     * Ghi chú lý do điều chỉnh (optional)
     */
    private String note;
}

