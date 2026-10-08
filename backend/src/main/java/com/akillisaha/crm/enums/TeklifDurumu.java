package com.akillisaha.crm.enums;

/**
 * Sahada açılan ticari teklif ve fırsatların durumlarını temsil eder.
 * Satış boru hattının (pipeline) ve kazanım oranlarının takibinde kullanılır.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
public enum TeklifDurumu {

    /**
     * Müşteriye teklif iletildi, karar veya pazarlık aşaması devam ediyor.
     */
    ACIK,

    /**
     * Teklif müşteri tarafından onaylandı, satış başarıyla kapandı.
     */
    KAZANILDI,

    /**
     * Müşteri rakip fiyat veya şartlar nedeniyle teklifi kabul etmedi.
     */
    KAYBEDILDI,

    /**
     * Müşteri ihtiyacı ortadan kalktığı veya vazgeçtiği için teklif iptal edildi.
     */
    IPTAL
}
