package com.akillisaha.crm.dto.request;

import com.akillisaha.crm.enums.OfferStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferRequest {

    @NotNull(message = "Firma seçimi zorunludur")
    private Long companyId;

    private Long visitId;

    @NotBlank(message = "Teklif başlığı zorunludur")
    private String title;

    private BigDecimal amount;

    @Builder.Default
    private String currency = "TRY";

    @Builder.Default
    private OfferStatus status = OfferStatus.ACIK;

    private String notes;
    private OffsetDateTime validUntil;
}
