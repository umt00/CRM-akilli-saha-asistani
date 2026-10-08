package com.akillisaha.crm.dto.request;

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
public class VisitRequest {

    @NotNull(message = "Firma seçimi zorunludur")
    private Long companyId;

    private Long contactId;

    @NotNull(message = "Ziyaret tarihi zorunludur")
    private OffsetDateTime visitDate;

    @NotBlank(message = "Ziyaret konusu zorunludur")
    private String topic;

    private String suppliedProducts;
    private String monthlyConsumption;
    private String purchasedProducts;
    private String currentSupplierCompetitor;

    @Builder.Default
    private Boolean hasSample = false;

    @Builder.Default
    private Boolean hasOffer = false;

    private String nextAction;
    private OffsetDateTime nextVisitDate;
    private String notes;

    // Ziyaret anında numune verilmişse eklenecek alanlar
    private String sampleProductName;
    private String sampleBrand;
    private String sampleQuantity;
    private String sampleResultNotes;

    // Ziyaret anında teklif açılmışsa eklenecek alanlar
    private String offerTitle;
    private java.math.BigDecimal offerAmount;
    private String offerCurrency;
}
