package com.akillisaha.crm.dto.request;

import com.akillisaha.crm.enums.SampleStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SampleRequest {

    private Long visitId;

    @NotNull(message = "Firma seçimi zorunludur")
    private Long companyId;

    private Long productId;

    @NotBlank(message = "Ürün adı zorunludur")
    private String productName;

    private String brand;

    @NotBlank(message = "Miktar zorunludur (örn: 250 GR)")
    private String quantity;

    @Builder.Default
    private SampleStatus status = SampleStatus.BEKLEMEDE;

    private String resultNotes;
    private OffsetDateTime sentDate;
    private OffsetDateTime evaluatedDate;
}
