package com.portfolio.auth.dto.request;

import com.portfolio.auth.model.Role;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating user role
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoleRequest {
    
    @NotNull(message = "Role is required")
    private Role role;
}
