package com.akillisaha.crm.dto.response;

import com.akillisaha.crm.enums.SampleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SampleResponse {

    private Long id;
    private Long visitId;
    private Long companyId;
    private String companyName;
    private Long representativeId;
    private String representativeName;
    private Long productId;
    private String productName;
    private String brand;
    private String quantity;
    private SampleStatus status;
    private String resultNotes;
    private OffsetDateTime sentDate;
    private OffsetDateTime evaluatedDate;
    private OffsetDateTime createdAt;
}
