package com.akillisaha.crm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactResponse {

    private Long id;
    private Long companyId;
    private String fullName;
    private String departmentRole;
    private String phone;
    private String email;
    private String notes;
    private OffsetDateTime createdAt;
}
