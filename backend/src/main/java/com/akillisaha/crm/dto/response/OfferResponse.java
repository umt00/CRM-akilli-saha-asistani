package com.akillisaha.crm.dto.response;

import com.akillisaha.crm.enums.OfferStatus;
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
public class OfferResponse {

    private Long id;
    private Long companyId;
    private String companyName;
    private Long visitId;
    private Long representativeId;
    private String representativeName;
    private String title;
    private BigDecimal amount;
    private String currency;
    private OfferStatus status;
    private String notes;
    private OffsetDateTime validUntil;
    private OffsetDateTime createdAt;
}
