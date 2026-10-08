package com.akillisaha.crm.service.impl;

import com.akillisaha.crm.dto.response.ExcelImportResultResponse;
import com.akillisaha.crm.entity.*;
import com.akillisaha.crm.enums.OfferStatus;
import com.akillisaha.crm.enums.Role;
import com.akillisaha.crm.enums.SampleStatus;
import com.akillisaha.crm.exception.BadRequestException;
import com.akillisaha.crm.exception.ResourceNotFoundException;
import com.akillisaha.crm.repository.*;
import com.akillisaha.crm.security.CustomUserDetails;
import com.akillisaha.crm.service.ExcelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelServiceImpl implements ExcelService {

    private final VisitRepository visitRepository;
    private final CompanyRepository companyRepository;
    private final CompanyContactRepository contactRepository;
    private final UserRepository userRepository;
    private final SampleRepository sampleRepository;
    private final OfferRepository offerRepository;

    private static final String[] HEADERS = {
            "Müşteri Adı",              // 0
            "Ziyaret Edilen Kişi",       // 1
            "Görevi / Departmanı",       // 2
            "Bölge / Şehir",             // 3
            "Ziyaret Tarihi",            // 4
            "Tedarik Ettiği Ürünler",    // 5
            "Aylık Kullanım Miktarı",    // 6
            "Bizden Aldığı Ürünler",     // 7
            "Mevcut Tedarikçi / Rakip",  // 8
            "Ziyaret Konusu",            // 9
            "Numune Verildi mi?",        // 10
            "Numune Sonucu",             // 11
            "Teklif Verildi mi?",        // 12
            "Teklif Tutarı",             // 13
            "Sonraki Aksiyon",           // 14
            "Sonraki Ziyaret Tarihi",    // 15
            "Notlar"                     // 16
    };

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    @Transactional(readOnly = true)
    public byte[] exportVisitsToExcel(CustomUserDetails currentUser, Long representativeId, OffsetDateTime startDate, OffsetDateTime endDate) {
        List<Visit> visits;

        if (currentUser.getRole() == Role.ADMIN) {
            if (representativeId != null) {
                visits = visitRepository.findByRepresentativeId(representativeId);
            } else {
                visits = visitRepository.findAll();
            }
        } else {
            // Veri İzolasyonu: Plasiyer yalnızca kendi ziyaretlerini indirebilir
            visits = visitRepository.findByRepresentativeId(currentUser.getId());
        }

        // Tarih filtresi
        if (startDate != null && endDate != null) {
            visits = visits.stream()
                    .filter(v -> !v.getVisitDate().isBefore(startDate) && !v.getVisitDate().isAfter(endDate))
                    .collect(Collectors.toList());
        }

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            SXSSFSheet sheet = workbook.createSheet("Müşteri Ziyaretleri");
            sheet.trackAllColumnsForAutoSizing();

            // 1. Başlık Stili (Kurumsal Koyu Lacivert ve Beyaz Kalın Metin)
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setFontHeightInPoints((short) 11);

            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
            headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerCellStyle.setBorderBottom(BorderStyle.THIN);
            headerCellStyle.setBorderTop(BorderStyle.THIN);
            headerCellStyle.setBorderLeft(BorderStyle.THIN);
            headerCellStyle.setBorderRight(BorderStyle.THIN);

            // 2. Veri Hücre Stilleri
            CellStyle dataCellStyle = workbook.createCellStyle();
            dataCellStyle.setBorderBottom(BorderStyle.THIN);
            dataCellStyle.setBorderTop(BorderStyle.THIN);
            dataCellStyle.setBorderLeft(BorderStyle.THIN);
            dataCellStyle.setBorderRight(BorderStyle.THIN);

            CellStyle dateCellStyle = workbook.createCellStyle();
            dateCellStyle.cloneStyleFrom(dataCellStyle);
            dateCellStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle numberCellStyle = workbook.createCellStyle();
            numberCellStyle.cloneStyleFrom(dataCellStyle);
            numberCellStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

            // Başlık Satırını Oluştur
            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(28);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerCellStyle);
            }

            // Veri Satırlarını Yaz
            int rowIdx = 1;
            for (Visit v : visits) {
                Row row = sheet.createRow(rowIdx++);

                // Kolon 0: Müşteri Adı
                createCell(row, 0, v.getCompany().getName(), dataCellStyle);

                // Kolon 1: Ziyaret Edilen Kişi
                createCell(row, 1, v.getContact() != null ? v.getContact().getFullName() : "", dataCellStyle);

                // Kolon 2: Görevi / Departmanı
                createCell(row, 2, v.getContact() != null ? v.getContact().getDepartmentRole() : "", dataCellStyle);

                // Kolon 3: Bölge / Şehir
                createCell(row, 3, v.getCompany().getCityRegion(), dataCellStyle);

                // Kolon 4: Ziyaret Tarihi
                createCell(row, 4, v.getVisitDate() != null ? v.getVisitDate().format(DATE_FORMATTER) : "", dateCellStyle);

                // Kolon 5: Tedarik Ettiği Ürünler
                createCell(row, 5, v.getSuppliedProducts() != null ? v.getSuppliedProducts() : v.getCompany().getSuppliedProducts(), dataCellStyle);

                // Kolon 6: Aylık Kullanım Miktarı
                createCell(row, 6, v.getMonthlyConsumption() != null ? v.getMonthlyConsumption() : v.getCompany().getMonthlyConsumption(), dataCellStyle);

                // Kolon 7: Bizden Aldığı Ürünler
                createCell(row, 7, v.getPurchasedProducts() != null ? v.getPurchasedProducts() : v.getCompany().getPurchasedProducts(), dataCellStyle);

                // Kolon 8: Mevcut Tedarikçi / Rakip
                createCell(row, 8, v.getCurrentSupplierCompetitor() != null ? v.getCurrentSupplierCompetitor() : v.getCompany().getCurrentSupplierCompetitor(), dataCellStyle);

                // Kolon 9: Ziyaret Konusu
                createCell(row, 9, v.getTopic(), dataCellStyle);

                // Kolon 10: Numune Verildi mi?
                createCell(row, 10, Boolean.TRUE.equals(v.getHasSample()) ? "Evet" : "Hayır", dateCellStyle);

                // Kolon 11: Numune Sonucu
                String sampleResult = "";
                if (!v.getSamples().isEmpty()) {
                    Sample firstSample = v.getSamples().get(0);
                    sampleResult = firstSample.getStatus().name() + (firstSample.getResultNotes() != null ? " (" + firstSample.getResultNotes() + ")" : "");
                }
                createCell(row, 11, sampleResult, dataCellStyle);

                // Kolon 12: Teklif Verildi mi?
                createCell(row, 12, Boolean.TRUE.equals(v.getHasOffer()) ? "Evet" : "Hayır", dateCellStyle);

                // Kolon 13: Teklif Tutarı
                if (!v.getOffers().isEmpty() && v.getOffers().get(0).getAmount() != null) {
                    Cell amountCell = row.createCell(13);
                    amountCell.setCellValue(v.getOffers().get(0).getAmount().doubleValue());
                    amountCell.setCellStyle(numberCellStyle);
                } else {
                    createCell(row, 13, "", dataCellStyle);
                }

                // Kolon 14: Sonraki Aksiyon
                createCell(row, 14, v.getNextAction(), dataCellStyle);

                // Kolon 15: Sonraki Ziyaret Tarihi
                createCell(row, 15, v.getNextVisitDate() != null ? v.getNextVisitDate().format(DATE_FORMATTER) : "", dateCellStyle);

                // Kolon 16: Notlar
                createCell(row, 16, v.getNotes(), dataCellStyle);
            }

            // Kolon Genişliklerini Ayarla
            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
                int currentWidth = sheet.getColumnWidth(i);
                sheet.setColumnWidth(i, Math.min(Math.max(currentWidth + 1200, 3500), 12000));
            }

            workbook.write(out);
            log.info("Excel export tamamlandı. Toplam kayıt: {}", visits.size());
            return out.toByteArray();

        } catch (Exception e) {
            log.error("Excel oluşturulurken hata: ", e);
            throw new RuntimeException("Excel dosyası üretilemedi: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ExcelImportResultResponse importVisitsFromExcel(MultipartFile file, CustomUserDetails currentUser) {
        if (file.isEmpty()) {
            throw new BadRequestException("Yüklenen dosya boş olamaz");
        }

        User representative = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + currentUser.getId()));

        int totalRows = 0;
        int newCompanies = 0;
        int visitsCreated = 0;
        int samplesCreated = 0;
        int offersCreated = 0;
        List<String> errors = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // İlk satır (Başlık) atlanır
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            int rowNumber = 1;
            while (rowIterator.hasNext()) {
                rowNumber++;
                Row row = rowIterator.next();

                String companyName = getCellValueAsString(row.getCell(0));
                if (!StringUtils.hasText(companyName)) {
                    // Müşteri adı boş satırları atla
                    continue;
                }

                totalRows++;

                try {
                    String contactName = getCellValueAsString(row.getCell(1));
                    String departmentRole = getCellValueAsString(row.getCell(2));
                    String cityRegion = getCellValueAsString(row.getCell(3));
                    String visitDateStr = getCellValueAsString(row.getCell(4));
                    String suppliedProducts = getCellValueAsString(row.getCell(5));
                    String monthlyConsumption = getCellValueAsString(row.getCell(6));
                    String purchasedProducts = getCellValueAsString(row.getCell(7));
                    String competitor = getCellValueAsString(row.getCell(8));
                    String topic = getCellValueAsString(row.getCell(9));
                    String hasSampleStr = getCellValueAsString(row.getCell(10));
                    String sampleResult = getCellValueAsString(row.getCell(11));
                    String hasOfferStr = getCellValueAsString(row.getCell(12));
                    String offerAmountStr = getCellValueAsString(row.getCell(13));
                    String nextAction = getCellValueAsString(row.getCell(14));
                    String nextVisitDateStr = getCellValueAsString(row.getCell(15));
                    String notes = getCellValueAsString(row.getCell(16));

                    // 1. Firma Kontrolü veya Oluşturma
                    Optional<Company> existingCompany = companyRepository.findByNameIgnoreCase(companyName.trim());
                    Company company;
                    if (existingCompany.isPresent()) {
                        company = existingCompany.get();
                    } else {
                        company = Company.builder()
                                .name(companyName.trim())
                                .cityRegion(cityRegion)
                                .currentSupplierCompetitor(competitor)
                                .suppliedProducts(suppliedProducts)
                                .monthlyConsumption(monthlyConsumption)
                                .purchasedProducts(purchasedProducts)
                                .assignedUser(representative)
                                .build();
                        company = companyRepository.save(company);
                        newCompanies++;
                    }

                    // 2. Yetkili Kişi Kontrolü
                    CompanyContact contact = null;
                    if (StringUtils.hasText(contactName)) {
                        Optional<CompanyContact> existingContact = company.getContacts().stream()
                                .filter(c -> c.getFullName().equalsIgnoreCase(contactName.trim()))
                                .findFirst();
                        if (existingContact.isPresent()) {
                            contact = existingContact.get();
                        } else {
                            contact = CompanyContact.builder()
                                    .company(company)
                                    .fullName(contactName.trim())
                                    .departmentRole(departmentRole)
                                    .build();
                            contact = contactRepository.save(contact);
                            company.getContacts().add(contact);
                        }
                    }

                    // 3. Tarih Ayrıştırma
                    OffsetDateTime visitDate = parseDateOrNow(visitDateStr);
                    OffsetDateTime nextVisitDate = StringUtils.hasText(nextVisitDateStr) ? parseDateOrNull(nextVisitDateStr) : null;

                    boolean hasSample = isTruthy(hasSampleStr);
                    boolean hasOffer = isTruthy(hasOfferStr);

                    // 4. Ziyareti Kaydet
                    Visit visit = Visit.builder()
                            .company(company)
                            .contact(contact)
                            .representative(representative)
                            .visitDate(visitDate)
                            .topic(StringUtils.hasText(topic) ? topic : "Rutin Ziyaret")
                            .suppliedProducts(suppliedProducts)
                            .monthlyConsumption(monthlyConsumption)
                            .purchasedProducts(purchasedProducts)
                            .currentSupplierCompetitor(competitor)
                            .hasSample(hasSample)
                            .hasOffer(hasOffer)
                            .nextAction(nextAction)
                            .nextVisitDate(nextVisitDate)
                            .notes(notes)
                            .build();
                    Visit savedVisit = visitRepository.save(visit);
                    visitsCreated++;

                    // 5. Numune Kaydı Aç
                    if (hasSample) {
                        Sample sample = Sample.builder()
                                .visit(savedVisit)
                                .company(company)
                                .representative(representative)
                                .productName("Excel Numunesi: " + (StringUtils.hasText(topic) ? topic : companyName))
                                .quantity("1 ADET")
                                .status(SampleStatus.BEKLEMEDE)
                                .resultNotes(sampleResult)
                                .sentDate(visitDate)
                                .build();
                        sampleRepository.save(sample);
                        samplesCreated++;
                    }

                    // 6. Teklif Kaydı Aç
                    if (hasOffer) {
                        BigDecimal amount = null;
                        if (StringUtils.hasText(offerAmountStr)) {
                            try {
                                amount = new BigDecimal(offerAmountStr.replaceAll("[^0-9.,]", "").replace(",", "."));
                            } catch (Exception ignored) {}
                        }

                        Offer offer = Offer.builder()
                                .company(company)
                                .visit(savedVisit)
                                .representative(representative)
                                .title("Excel Teklifi: " + companyName)
                                .amount(amount)
                                .currency("TRY")
                                .status(OfferStatus.ACIK)
                                .build();
                        offerRepository.save(offer);
                        offersCreated++;
                    }

                } catch (Exception rowEx) {
                    String error = "Satır " + rowNumber + " (" + companyName + ") işlenirken hata: " + rowEx.getMessage();
                    log.warn(error);
                    errors.add(error);
                }
            }

        } catch (Exception e) {
            log.error("Excel import dosyası okunamadı: ", e);
            throw new BadRequestException("Excel dosyası işlenirken hata oluştu: " + e.getMessage());
        }

        log.info("Excel Import tamamlandı: Toplam={}, YeniFirma={}, Ziyaret={}, Hatalar={}",
                totalRows, newCompanies, visitsCreated, errors.size());

        return ExcelImportResultResponse.builder()
                .totalRowsProcessed(totalRows)
                .newCompaniesCreated(newCompanies)
                .visitsCreated(visitsCreated)
                .samplesCreated(samplesCreated)
                .offersCreated(offersCreated)
                .errorMessages(errors)
                .build();
    }

    private void createCell(Row row, int colIdx, String value, CellStyle style) {
        Cell cell = row.createCell(colIdx);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                } else {
                    double val = cell.getNumericCellValue();
                    if (val == Math.floor(val)) {
                        yield String.valueOf((long) val);
                    }
                    yield String.valueOf(val);
                }
            }
            case BOOLEAN -> cell.getBooleanCellValue() ? "Evet" : "Hayır";
            case FORMULA -> {
                try {
                    yield cell.getStringCellValue().trim();
                } catch (Exception e) {
                    yield String.valueOf(cell.getNumericCellValue());
                }
            }
            default -> "";
        };
    }

    private boolean isTruthy(String str) {
        if (!StringUtils.hasText(str)) return false;
        String val = str.trim().toLowerCase();
        return val.equals("evet") || val.equals("var") || val.equals("true") || val.equals("1") || val.equals("x");
    }

    private OffsetDateTime parseDateOrNow(String dateStr) {
        OffsetDateTime parsed = parseDateOrNull(dateStr);
        return parsed != null ? parsed : OffsetDateTime.now();
    }

    private OffsetDateTime parseDateOrNull(String dateStr) {
        if (!StringUtils.hasText(dateStr)) return null;
        try {
            LocalDate localDate = LocalDate.parse(dateStr.trim(), DATE_FORMATTER);
            return localDate.atStartOfDay().atOffset(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
