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
public class VisitResponse {

    private Long id;
    private Long companyId;
    private String companyName;
    private String cityRegion;
    private Long contactId;
    private String contactName;
    private String contactDepartmentRole;
    private Long representativeId;
    private String representativeName;
    private OffsetDateTime visitDate;
    private String topic;
    private String suppliedProducts;
    private String monthlyConsumption;
    private String purchasedProducts;
    private String currentSupplierCompetitor;
    private Boolean hasSample;
    private Boolean hasOffer;
    private String nextAction;
    private OffsetDateTime nextVisitDate;
    private String notes;
    private List<SampleResponse> samples;
    private List<OfferResponse> offers;
    private OffsetDateTime createdAt;
}
