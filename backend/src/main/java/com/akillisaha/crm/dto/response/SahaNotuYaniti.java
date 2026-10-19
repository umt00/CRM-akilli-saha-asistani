package com.akillisaha.crm.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * WhatsApp saha notunun çözümlenip CRM'e kaydedilmesi sonrası dönen operasyonel yanıt.
 * n8n bu yanıtı WhatsApp'tan plasiyere teyit mesajı olarak iletebilir.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@Getter
@Setter
public class SahaNotuYaniti {

    private boolean basarili;
    private String teyitMesaji;
    private Long firmaId;
    private String firmaUnvani;
    private String yetkiliAdi;
    private Long ziyaretId;
    private Long numuneId;
    private String numuneDetayi;
    private Long teklifId;
    private String teklifDetayi;
    private Map<String, Object> cikarilanBilgiler = new HashMap<>();

    public SahaNotuYaniti() {
    }

    public SahaNotuYaniti(boolean basarili, String teyitMesaji) {
        this.basarili = basarili;
        this.teyitMesaji = teyitMesaji;
    }
}
