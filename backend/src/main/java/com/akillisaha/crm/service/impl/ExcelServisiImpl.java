package com.akillisaha.crm.service.impl;

import com.akillisaha.crm.dto.response.ExcelIceAktarimSonucuYaniti;
import com.akillisaha.crm.entity.*;
import com.akillisaha.crm.enums.NumuneDurumu;
import com.akillisaha.crm.enums.Rol;
import com.akillisaha.crm.enums.TeklifDurumu;
import com.akillisaha.crm.exception.GecersizIstekHatasi;
import com.akillisaha.crm.exception.KaynakBulunamadiHatasi;
import com.akillisaha.crm.repository.*;
import com.akillisaha.crm.security.OzelKullaniciDetaylari;
import com.akillisaha.crm.service.ExcelServisi;
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

/**
 * Apache POI Excel işleme servisinin somut implementasyonu.
 * 17 kolonluk Excel şablonunu Streaming SXSSFWorkbook ile üretir ve satır satır içe aktarır.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelServisiImpl implements ExcelServisi {

    private final ZiyaretRepository ziyaretRepository;
    private final FirmaRepository firmaRepository;
    private final FirmaYetkiliRepository yetkiliRepository;
    private final KullaniciRepository kullaniciRepository;
    private final NumuneRepository numuneRepository;
    private final TeklifRepository teklifRepository;

    /**
     * Sahadaki standart 17 kolonluk Excel başlıkları listesi.
     */
    private static final String[] KOLON_BASLIKLARI = {
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

    private static final DateTimeFormatter TARIH_BICIMLEYICI = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    @Transactional(readOnly = true)
    public byte[] ziyaretleriExceleAktar(OzelKullaniciDetaylari aktifKullanici, Long plasiyerId, OffsetDateTime baslangicTarihi, OffsetDateTime bitisTarihi) {
        List<Ziyaret> ziyaretler;

        if (aktifKullanici.getRol() == Rol.YONETICI) {
            if (plasiyerId != null) {
                ziyaretler = ziyaretRepository.findByPlasiyerId(plasiyerId);
            } else {
                ziyaretler = ziyaretRepository.findAll();
            }
        } else {
            // Veri İzolasyonu: Plasiyer yalnızca kendi ziyaretlerini dışa aktarabilir
            ziyaretler = ziyaretRepository.findByPlasiyerId(aktifKullanici.getId());
        }

        // Tarih filtresi uygula
        if (baslangicTarihi != null && bitisTarihi != null) {
            ziyaretler = ziyaretler.stream()
                    .filter(z -> !z.getZiyaretTarihi().isBefore(baslangicTarihi) && !z.getZiyaretTarihi().isAfter(bitisTarihi))
                    .collect(Collectors.toList());
        }

        try (SXSSFWorkbook calismaKitabi = new SXSSFWorkbook(100);
             ByteArrayOutputStream ciktiAkisi = new ByteArrayOutputStream()) {

            SXSSFSheet sayfa = calismaKitabi.createSheet("Müşteri Ziyaretleri");
            sayfa.trackAllColumnsForAutoSizing();

            // 1. Kurumsal Başlık Hücre Stili (Koyu Lacivert & Beyaz Kalın Metin)
            Font baslikYazitipi = calismaKitabi.createFont();
            baslikYazitipi.setBold(true);
            baslikYazitipi.setColor(IndexedColors.WHITE.getIndex());
            baslikYazitipi.setFontHeightInPoints((short) 11);

            CellStyle baslikStili = calismaKitabi.createCellStyle();
            baslikStili.setFont(baslikYazitipi);
            baslikStili.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            baslikStili.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            baslikStili.setAlignment(HorizontalAlignment.CENTER);
            baslikStili.setVerticalAlignment(VerticalAlignment.CENTER);
            baslikStili.setBorderBottom(BorderStyle.THIN);
            baslikStili.setBorderTop(BorderStyle.THIN);
            baslikStili.setBorderLeft(BorderStyle.THIN);
            baslikStili.setBorderRight(BorderStyle.THIN);

            // 2. Veri Satırı Hücre Stilleri
            CellStyle veriStili = calismaKitabi.createCellStyle();
            veriStili.setBorderBottom(BorderStyle.THIN);
            veriStili.setBorderTop(BorderStyle.THIN);
            veriStili.setBorderLeft(BorderStyle.THIN);
            veriStili.setBorderRight(BorderStyle.THIN);

            CellStyle tarihStili = calismaKitabi.createCellStyle();
            tarihStili.cloneStyleFrom(veriStili);
            tarihStili.setAlignment(HorizontalAlignment.CENTER);

            CellStyle paraStili = calismaKitabi.createCellStyle();
            paraStili.cloneStyleFrom(veriStili);
            paraStili.setDataFormat(calismaKitabi.createDataFormat().getFormat("#,##0.00"));

            // Başlık Satırını Yaz
            Row baslikSatiri = sayfa.createRow(0);
            baslikSatiri.setHeightInPoints(28);
            for (int i = 0; i < KOLON_BASLIKLARI.length; i++) {
                Cell hucre = baslikSatiri.createCell(i);
                hucre.setCellValue(KOLON_BASLIKLARI[i]);
                hucre.setCellStyle(baslikStili);
            }

            // Veri Satırlarını Doldur
            int satirNo = 1;
            for (Ziyaret z : ziyaretler) {
                Row satir = sayfa.createRow(satirNo++);

                // Kolon 0: Müşteri Adı
                hucreOlustur(satir, 0, z.getFirma().getUnvan(), veriStili);

                // Kolon 1: Ziyaret Edilen Kişi
                hucreOlustur(satir, 1, z.getYetkili() != null ? z.getYetkili().getAdSoyad() : "", veriStili);

                // Kolon 2: Görevi / Departmanı
                hucreOlustur(satir, 2, z.getYetkili() != null ? z.getYetkili().getUnvanGorev() : "", veriStili);

                // Kolon 3: Bölge / Şehir
                hucreOlustur(satir, 3, z.getFirma().getSehirBolge(), veriStili);

                // Kolon 4: Ziyaret Tarihi
                hucreOlustur(satir, 4, z.getZiyaretTarihi() != null ? z.getZiyaretTarihi().format(TARIH_BICIMLEYICI) : "", tarihStili);

                // Kolon 5: Tedarik Ettiği Ürünler
                hucreOlustur(satir, 5, z.getTedarikEttigiUrunler() != null ? z.getTedarikEttigiUrunler() : z.getFirma().getTedarikEttigiUrunler(), veriStili);

                // Kolon 6: Aylık Kullanım Miktarı
                hucreOlustur(satir, 6, z.getAylikKullanimMiktari() != null ? z.getAylikKullanimMiktari() : z.getFirma().getAylikKullanimMiktari(), veriStili);

                // Kolon 7: Bizden Aldığı Ürünler
                hucreOlustur(satir, 7, z.getBizdenAldigiUrunler() != null ? z.getBizdenAldigiUrunler() : z.getFirma().getBizdenAldigiUrunler(), veriStili);

                // Kolon 8: Mevcut Tedarikçi / Rakip
                hucreOlustur(satir, 8, z.getMevcutTedarikciRakip() != null ? z.getMevcutTedarikciRakip() : z.getFirma().getMevcutTedarikciRakip(), veriStili);

                // Kolon 9: Ziyaret Konusu
                hucreOlustur(satir, 9, z.getZiyaretKonusu(), veriStili);

                // Kolon 10: Numune Verildi mi?
                hucreOlustur(satir, 10, Boolean.TRUE.equals(z.getNumuneVerildiMi()) ? "Evet" : "Hayır", tarihStili);

                // Kolon 11: Numune Sonucu
                String numuneSonucu = "";
                if (!z.getNumuneler().isEmpty()) {
                    Numune ilkNumune = z.getNumuneler().get(0);
                    numuneSonucu = ilkNumune.getDurum().name() + (ilkNumune.getSonucNotlari() != null ? " (" + ilkNumune.getSonucNotlari() + ")" : "");
                }
                hucreOlustur(satir, 11, numuneSonucu, veriStili);

                // Kolon 12: Teklif Verildi mi?
                hucreOlustur(satir, 12, Boolean.TRUE.equals(z.getTeklifVerildiMi()) ? "Evet" : "Hayır", tarihStili);

                // Kolon 13: Teklif Tutarı
                if (!z.getTeklifler().isEmpty() && z.getTeklifler().get(0).getTutar() != null) {
                    Cell tutarHucresi = satir.createCell(13);
                    tutarHucresi.setCellValue(z.getTeklifler().get(0).getTutar().doubleValue());
                    tutarHucresi.setCellStyle(paraStili);
                } else {
                    hucreOlustur(satir, 13, "", veriStili);
                }

                // Kolon 14: Sonraki Aksiyon
                hucreOlustur(satir, 14, z.getSonrakiAksiyon(), veriStili);

                // Kolon 15: Sonraki Ziyaret Tarihi
                hucreOlustur(satir, 15, z.getSonrakiZiyaretTarihi() != null ? z.getSonrakiZiyaretTarihi().format(TARIH_BICIMLEYICI) : "", tarihStili);

                // Kolon 16: Notlar
                hucreOlustur(satir, 16, z.getNotlar(), veriStili);
            }

            // Kolon Genişliklerini Otomatik Ayarla
            for (int i = 0; i < KOLON_BASLIKLARI.length; i++) {
                sayfa.autoSizeColumn(i);
                int mevcutGenislik = sayfa.getColumnWidth(i);
                sayfa.setColumnWidth(i, Math.min(Math.max(mevcutGenislik + 1200, 3500), 12000));
            }

            calismaKitabi.write(ciktiAkisi);
            log.info("Excel dışa aktarım (export) tamamlandı. Toplam satır: {}", ziyaretler.size());
            return ciktiAkisi.toByteArray();

        } catch (Exception istisna) {
            log.error("Excel dosyası oluşturulurken hata: ", istisna);
            throw new RuntimeException("Excel dosyası üretilemedi: " + istisna.getMessage());
        }
    }

    @Override
    @Transactional
    public ExcelIceAktarimSonucuYaniti exceldenZiyaretleriIceAktar(MultipartFile dosya, OzelKullaniciDetaylari aktifKullanici) {
        if (dosya.isEmpty()) {
            throw new GecersizIstekHatasi("Yüklenen Excel dosyası boş olamaz");
        }

        Kullanici plasiyer = kullaniciRepository.findById(aktifKullanici.getId())
                .orElseThrow(() -> new KaynakBulunamadiHatasi("Kullanıcı bulunamadı: " + aktifKullanici.getId()));

        int toplamSatir = 0;
        int yeniFirmaSayisi = 0;
        int ziyaretSayisi = 0;
        int numuneSayisi = 0;
        int teklifSayisi = 0;
        List<String> hataListesi = new ArrayList<>();

        try (InputStream girdiAkisi = dosya.getInputStream();
             Workbook calismaKitabi = WorkbookFactory.create(girdiAkisi)) {

            Sheet sayfa = calismaKitabi.getSheetAt(0);
            Iterator<Row> satirGezgini = sayfa.iterator();

            // Başlık satırını atla
            if (satirGezgini.hasNext()) {
                satirGezgini.next();
            }

            int satirSayaci = 1;
            while (satirGezgini.hasNext()) {
                satirSayaci++;
                Row satir = satirGezgini.next();

                String firmaUnvani = hucreMetniniAl(satir.getCell(0));
                if (!StringUtils.hasText(firmaUnvani)) {
                    continue; // Boş satırları atla
                }

                toplamSatir++;

                try {
                    String yetkiliAdi = hucreMetniniAl(satir.getCell(1));
                    String unvanGorev = hucreMetniniAl(satir.getCell(2));
                    String sehirBolge = hucreMetniniAl(satir.getCell(3));
                    String ziyaretTarihiMetni = hucreMetniniAl(satir.getCell(4));
                    String tedarikUrunleri = hucreMetniniAl(satir.getCell(5));
                    String aylikKullanim = hucreMetniniAl(satir.getCell(6));
                    String bizdenAldigi = hucreMetniniAl(satir.getCell(7));
                    String rakip = hucreMetniniAl(satir.getCell(8));
                    String konu = hucreMetniniAl(satir.getCell(9));
                    String numuneVarMi = hucreMetniniAl(satir.getCell(10));
                    String numuneSonucu = hucreMetniniAl(satir.getCell(11));
                    String teklifVarMi = hucreMetniniAl(satir.getCell(12));
                    String teklifTutariMetni = hucreMetniniAl(satir.getCell(13));
                    String sonrakiAksiyon = hucreMetniniAl(satir.getCell(14));
                    String sonrakiTarihMetni = hucreMetniniAl(satir.getCell(15));
                    String notlar = hucreMetniniAl(satir.getCell(16));

                    // 1. Firma Kontrolü veya Yeni Kayıt
                    Optional<Firma> mevcutFirma = firmaRepository.findByUnvanIgnoreCase(firmaUnvani.trim());
                    Firma firma;
                    if (mevcutFirma.isPresent()) {
                        firma = mevcutFirma.get();
                    } else {
                        firma = Firma.builder()
                                .unvan(firmaUnvani.trim())
                                .sehirBolge(sehirBolge)
                                .mevcutTedarikciRakip(rakip)
                                .tedarikEttigiUrunler(tedarikUrunleri)
                                .aylikKullanimMiktari(aylikKullanim)
                                .bizdenAldigiUrunler(bizdenAldigi)
                                .atananPlasiyer(plasiyer)
                                .build();
                        firma = firmaRepository.save(firma);
                        yeniFirmaSayisi++;
                    }

                    // 2. Yetkili Kişi Kontrolü
                    FirmaYetkili yetkili = null;
                    if (StringUtils.hasText(yetkiliAdi)) {
                        Optional<FirmaYetkili> mevcutYetkili = firma.getYetkililer().stream()
                                .filter(y -> y.getAdSoyad().equalsIgnoreCase(yetkiliAdi.trim()))
                                .findFirst();
                        if (mevcutYetkili.isPresent()) {
                            yetkili = mevcutYetkili.get();
                        } else {
                            yetkili = FirmaYetkili.builder()
                                    .firma(firma)
                                    .adSoyad(yetkiliAdi.trim())
                                    .unvanGorev(unvanGorev)
                                    .build();
                            yetkili = yetkiliRepository.save(yetkili);
                            firma.getYetkililer().add(yetkili);
                        }
                    }

                    // 3. Tarih Ayrıştırma
                    OffsetDateTime ziyaretTarihi = tarihiAyristirVeyaSimdi(ziyaretTarihiMetni);
                    OffsetDateTime sonrakiTarih = StringUtils.hasText(sonrakiTarihMetni) ? tarihiAyristirVeyaNull(sonrakiTarihMetni) : null;

                    boolean numuneVerildiMi = dogruMu(numuneVarMi);
                    boolean teklifVerildiMi = dogruMu(teklifVarMi);

                    // 4. Ziyareti Kaydet
                    Ziyaret ziyaret = Ziyaret.builder()
                            .firma(firma)
                            .yetkili(yetkili)
                            .plasiyer(plasiyer)
                            .ziyaretTarihi(ziyaretTarihi)
                            .ziyaretKonusu(StringUtils.hasText(konu) ? konu : "Rutin Saha Ziyareti")
                            .tedarikEttigiUrunler(tedarikUrunleri)
                            .aylikKullanimMiktari(aylikKullanim)
                            .bizdenAldigiUrunler(bizdenAldigi)
                            .mevcutTedarikciRakip(rakip)
                            .numuneVerildiMi(numuneVerildiMi)
                            .teklifVerildiMi(teklifVerildiMi)
                            .sonrakiAksiyon(sonrakiAksiyon)
                            .sonrakiZiyaretTarihi(sonrakiTarih)
                            .notlar(notlar)
                            .build();

                    Ziyaret kaydedilenZiyaret = ziyaretRepository.save(ziyaret);
                    ziyaretSayisi++;

                    // 5. Numune Kaydı Aç
                    if (numuneVerildiMi) {
                        Numune numune = Numune.builder()
                                .ziyaret(kaydedilenZiyaret)
                                .firma(firma)
                                .plasiyer(plasiyer)
                                .urunAdi("Excel Numunesi: " + (StringUtils.hasText(konu) ? konu : firmaUnvani))
                                .miktar("1 ADET")
                                .durum(NumuneDurumu.BEKLEMEDE)
                                .sonucNotlari(numuneSonucu)
                                .gonderimTarihi(ziyaretTarihi)
                                .build();
                        numuneRepository.save(numune);
                        numuneSayisi++;
                    }

                    // 6. Teklif Kaydı Aç
                    if (teklifVerildiMi) {
                        BigDecimal tutar = null;
                        if (StringUtils.hasText(teklifTutariMetni)) {
                            try {
                                tutar = new BigDecimal(teklifTutariMetni.replaceAll("[^0-9.,]", "").replace(",", "."));
                            } catch (Exception ignored) {}
                        }

                        Teklif teklif = Teklif.builder()
                                .firma(firma)
                                .ziyaret(kaydedilenZiyaret)
                                .plasiyer(plasiyer)
                                .baslik("Excel Teklifi: " + firmaUnvani)
                                .tutar(tutar)
                                .paraBirimi("TRY")
                                .durum(TeklifDurumu.ACIK)
                                .build();
                        teklifRepository.save(teklif);
                        teklifSayisi++;
                    }

                } catch (Exception satirHatasi) {
                    String hata = "Satır " + satirSayaci + " (" + firmaUnvani + ") işlenirken hata: " + satirHatasi.getMessage();
                    log.warn(hata);
                    hataListesi.add(hata);
                }
            }

        } catch (Exception e) {
            log.error("Excel içe aktarım dosyası okunamadı: ", e);
            throw new GecersizIstekHatasi("Excel dosyası işlenirken hata oluştu: " + e.getMessage());
        }

        log.info("Excel İçe Aktarım Tamamlandı: Toplam={}, YeniFirma={}, Ziyaret={}, Numune={}, Hatalar={}",
                toplamSatir, yeniFirmaSayisi, ziyaretSayisi, numuneSayisi, hataListesi.size());

        return ExcelIceAktarimSonucuYaniti.builder()
                .toplamSatirSayisi(toplamSatir)
                .yeniFirmaSayisi(yeniFirmaSayisi)
                .olusturulanZiyaretSayisi(ziyaretSayisi)
                .olusturulanNumuneSayisi(numuneSayisi)
                .olusturulanTeklifSayisi(teklifSayisi)
                .hataMesajlari(hataListesi)
                .build();
    }

    private void hucreOlustur(Row satir, int kolonIndeks, String deger, CellStyle stil) {
        Cell hucre = satir.createCell(kolonIndeks);
        hucre.setCellValue(deger != null ? deger : "");
        hucre.setCellStyle(stil);
    }

    private String hucreMetniniAl(Cell hucre) {
        if (hucre == null) {
            return "";
        }
        return switch (hucre.getCellType()) {
            case STRING -> hucre.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(hucre)) {
                    yield hucre.getLocalDateTimeCellValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                } else {
                    double deger = hucre.getNumericCellValue();
                    if (deger == Math.floor(deger)) {
                        yield String.valueOf((long) deger);
                    }
                    yield String.valueOf(deger);
                }
            }
            case BOOLEAN -> hucre.getBooleanCellValue() ? "Evet" : "Hayır";
            case FORMULA -> {
                try {
                    yield hucre.getStringCellValue().trim();
                } catch (Exception e) {
                    yield String.valueOf(hucre.getNumericCellValue());
                }
            }
            default -> "";
        };
    }

    private boolean dogruMu(String deger) {
        if (!StringUtils.hasText(deger)) return false;
        String kucuk = deger.trim().toLowerCase();
        return kucuk.equals("evet") || kucuk.equals("var") || kucuk.equals("true") || kucuk.equals("1") || kucuk.equals("x");
    }

    private OffsetDateTime tarihiAyristirVeyaSimdi(String tarihMetni) {
        OffsetDateTime sonuc = tarihiAyristirVeyaNull(tarihMetni);
        return sonuc != null ? sonuc : OffsetDateTime.now();
    }

    private OffsetDateTime tarihiAyristirVeyaNull(String tarihMetni) {
        if (!StringUtils.hasText(tarihMetni)) return null;
        try {
            LocalDate yerelTarih = LocalDate.parse(tarihMetni.trim(), TARIH_BICIMLEYICI);
            return yerelTarih.atStartOfDay().atOffset(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
