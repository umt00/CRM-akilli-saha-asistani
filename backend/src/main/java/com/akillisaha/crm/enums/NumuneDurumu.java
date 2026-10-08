package com.akillisaha.crm.enums;

/**
 * Sahaya bırakılan yüksek maliyetli ürün numunelerinin yaşam döngüsü durumlarını temsil eder.
 * Numunelerin unutulmasını engelleyip siparişe dönüşüm oranlarını ölçmek için kullanılır.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
public enum NumuneDurumu {

    /**
     * Müşteriye numune teslim edildi, geri bildirim veya tadım bekleniyor.
     */
    BEKLEMEDE,

    /**
     * Müşteri Ar-Ge veya üretim hattında deneme/tadım aşamasında.
     */
    TEST_ASAMASINDA,

    /**
     * Müşteri numuneyi beğendi ve fiyat/teklif talep etti.
     */
    BEGENDI,

    /**
     * Müşteri lezzet, maliyet veya içerik sebebiyle numuneyi reddetti.
     */
    REDDETTI,

    /**
     * Numune süreci ticari başarıya ulaştı ve kesin siparişe dönüştü.
     */
    SIPARISE_DONUSTU
}
