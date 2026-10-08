package com.akillisaha.crm.dto.response;

import com.akillisaha.crm.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryResponse {

    private Long id;
    private String email;
    private String fullName;
    private Role role;
    private String phone;
    private Boolean isActive;
    private OffsetDateTime createdAt;
}
