package com.akillisaha.crm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 17 kolonluk Excel dosyası sisteme yüklendiğinde (Import) oluşan istatistik ve sonuç raporunu içeren DTO.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelIceAktarimSonucuYaniti {

    /**
     * Excel'de taranıp işlenen toplam satır adedi.
     */
    private int toplamSatirSayisi;

    /**
     * Veritabanında bulunmayıp yeni oluşturulan firma adedi.
     */
    private int yeniFirmaSayisi;

    /**
     * Sisteme başarıyla aktarılan ziyaret kaydı adedi.
     */
    private int olusturulanZiyaretSayisi;

    /**
     * Excel satırlarından otomatik açılan numune kartı sayısı.
     */
    private int olusturulanNumuneSayisi;

    /**
     * Excel satırlarından otomatik açılan teklif sayısı.
     */
    private int olusturulanTeklifSayisi;

    /**
     * Okuma veya ayrıştırma sırasında hata alınan satırların hata açıklamaları.
     */
    @Builder.Default
    private List<String> hataMesajlari = new ArrayList<>();
}
