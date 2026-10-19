package com.akillisaha.crm.service;

import com.akillisaha.crm.dto.request.SahaNotuIstegi;
import com.akillisaha.crm.dto.response.SahaNotuYaniti;

/**
 * WhatsApp ve n8n üzerinden gelen serbest saha notlarını kural tabanlı
 * NLP teknikleriyle ayrıştırıp CRM veri tabanına işleyen servis sözleşmesi.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
public interface SahaNotuServisi {

    /**
     * Ham metni çözümler, firma ve yetkiliyi tespit eder; ziyaret, numune ve teklif kayıtlarını açar.
     * 
     * @param istek Plasiyerin mesajını içeren istek gövdesi
     * @return Yapılan işlemlerin özeti ve WhatsApp teyit metni
     */
    SahaNotuYaniti sahaNotunuIsle(SahaNotuIstegi istek);
}
