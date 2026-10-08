package com.akillisaha.crm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyResponse {

    private Long id;
    private String name;
    private String cityRegion;
    private String address;
    private String phone;
    private String email;
    private String currentSupplierCompetitor;
    private String suppliedProducts;
    private String monthlyConsumption;
    private String purchasedProducts;
    private Long assignedUserId;
    private String assignedUserName;
    private List<ContactResponse> contacts;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
