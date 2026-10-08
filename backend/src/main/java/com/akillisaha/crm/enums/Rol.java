package com.akillisaha.crm.enums;

/**
 * Sistem içerisindeki kullanıcı rollerini belirten numaralandırma (Enum).
 * Rol Tabanlı Erişim Denetimi (RBAC) kapsamında yetkilendirme kararlarında kullanılır.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
public enum Rol {

    /**
     * Sistem Yöneticisi: Tüm firmaları, kullanıcıları ve raporları görme,
     * plasiyer atamalarını değiştirme tam yetkisine sahiptir.
     */
    YONETICI,

    /**
     * Saha Satış Temsilcisi (Plasiyer): Yalnızca kendisine atanan firmaları,
     * kendi gerçekleştirdiği ziyaretleri, bıraktığı numuneleri ve açtığı teklifleri görebilir.
     */
    PLASIYER
}
