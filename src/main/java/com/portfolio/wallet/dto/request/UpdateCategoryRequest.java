package com.portfolio.wallet.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating a category
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCategoryRequest {
    
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    private String name;
    
    @Size(max = 50, message = "Icon must not exceed 50 characters")
    private String icon;
    
    @Size(max = 7, message = "Color must be a valid hex color (e.g., #FF5733)")
    private String color;
}
