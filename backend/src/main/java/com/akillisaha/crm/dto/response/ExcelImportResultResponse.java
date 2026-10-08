package com.akillisaha.crm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelImportResultResponse {

    private int totalRowsProcessed;
    private int newCompaniesCreated;
    private int visitsCreated;
    private int samplesCreated;
    private int offersCreated;
    @Builder.Default
    private List<String> errorMessages = new ArrayList<>();
}
