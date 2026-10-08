package com.akillisaha.crm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactRequest {

    @NotNull(message = "Firma ID zorunludur")
    private Long companyId;

    @NotBlank(message = "Yetkili kişi adı zorunludur")
    @Size(max = 120, message = "Ad en fazla 120 karakter olabilir")
    private String fullName;

    @Size(max = 120, message = "Departman/Görev en fazla 120 karakter olabilir")
    private String departmentRole;

    private String phone;
    private String email;
    private String notes;
}
