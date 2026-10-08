package com.akillisaha.crm.service;

import com.akillisaha.crm.dto.response.ExcelImportResultResponse;
import com.akillisaha.crm.security.CustomUserDetails;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;

public interface ExcelService {

    byte[] exportVisitsToExcel(CustomUserDetails currentUser, Long representativeId, OffsetDateTime startDate, OffsetDateTime endDate);

    ExcelImportResultResponse importVisitsFromExcel(MultipartFile file, CustomUserDetails currentUser);
}
