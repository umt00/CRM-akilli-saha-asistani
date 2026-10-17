# 🚀 Akıllı Saha CRM - Kapsamlı API Kapıları & Entegrasyon Spesifikasyonu
> **Hedef Kitle:** 3. Kişi Frontend Geliştiricileri, Mobil Geliştiriciler, n8n Entegratörleri ve AI Ajanları (Antigravity / Cursor / Copilot).  
> **Sürüm:** `v1.0.0-PROD` | **Spring Boot:** `3.3.4` | **OpenAPI:** `3.0` | **Taban URL:** `/api/v1`

---

## 1. MİMARİ VE GENEL ENTEGRASYON KURALLARI

### 1.1. Temel Adresler (Base URLs)
* **Yerel Geliştirme (Localhost):** `http://localhost:8080/api/v1`
* **Vite Proxy (Yerel Frontend):** `/api/v1` (Port 5173 üzerinden `8080`'e ters vekil)
* **Swagger UI (İnteraktif Dokümantasyon):** `http://localhost:8080/swagger-ui.html`
* **OpenAPI Raw JSON:** `http://localhost:8080/v3/api-docs`
* **Sağlık Denetimi (Health Check):** `http://localhost:8080/actuator/health`

### 1.2. Standart HTTP Başlıkları (Headers)
Tüm isteklerde aksi belirtilmedikçe aşağıdaki başlıklar zorunludur:
```http
Content-Type: application/json
Accept: application/json
Authorization: Bearer <JWT_ERISIM_BELIRTECI>
```
*(Yalnızca `/api/v1/kimlik-dogrulama/giris` ve `/api/v1/kimlik-dogrulama/kayit` uç noktaları `Authorization` başlığı istemez).*

---

### 1.3. Standart API Yanıt Zarfı (`IslemSonucu<T>`)
Backend'den dönen tüm JSON yanıtları (hatalar dahil) standart `IslemSonucu` zarfı içerisinde paketlenir:

```json
{
  "basarili": true,
  "mesaj": "İşlem başarıyla tamamlandı",
  "veri": { ... },
  "zamanDamgasi": "2026-10-13T14:30:00.123+03:00"
}
```

* `basarili` (`boolean`): İşlemin mantıksal başarı durumu.
* `mesaj` (`string`): Kullanıcıya veya loglara gösterilecek Türkçe bilgi/hata mesajı.
* `veri` (`T | null`): Başarılı yanıtta DTO nesnesi, dizi veya sayfalama objesi; hatada `null` veya alan doğrulama haritası.
* `zamanDamgasi` (`ISO-8601 String`): Sunucu zamanı (`yyyy-MM-dd'T'HH:mm:ss.SSSXXX`).

---

### 1.4. Standart Sayfalama Yapısı (`Page<T>`)
Tüm liste (`GET`) uç noktaları Spring Data `Pageable` destekler.
* **Varsayılan Parametreler:** `?page=0&size=20&sort=olusturulmaTarihi,desc`
* **Yanıt Şeması (`veri` içeriği):**
```json
{
  "content": [ ... ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": { "sorted": true, "unsorted": false, "empty": false },
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalElements": 48,
  "totalPages": 3,
  "last": false,
  "first": true,
  "size": 20,
  "number": 0,
  "numberOfElements": 20,
  "empty": false
}
```

---

### 1.5. Hata Yönetimi & HTTP Durum Kodları
| Kod | Durum | Açıklama |
| :---: | :--- | :--- |
| **200 OK** | Başarılı | Kayıt çekme, güncelleme veya silme başarılı. |
| **201 CREATED** | Oluşturuldu | Yeni firma, ziyaret, teklif veya kullanıcı oluşturuldu. |
| **400 BAD REQUEST** | Hatalı İstek | Validasyon hatası (`@NotNull`, `@Size` vb.) veya eksik parametre. |
| **401 UNAUTHORIZED**| Yetkisiz | Belirteç eksik, geçersiz veya süresi dolmuş. |
| **403 FORBIDDEN**   | Erişim Yasak | Rol yetersizliği veya veri izolasyonu (başka temsilcinin kaydı). |
| **404 NOT FOUND**   | Bulunamadı | İstenen ID'ye sahip kayıt mevcut değil. |
| **500 SERVER ERROR**| Sunucu Hatası | Beklenmeyen sistem istisnası. |

**400 Validasyon Hatası Örneği:**
```json
{
  "basarili": false,
  "mesaj": "Girilen form alanları doğrulanamadı",
  "veri": {
    "unvan": "Firma unvanı zorunludur",
    "eposta": "Geçerli bir e-posta formatı giriniz"
  },
  "zamanDamgasi": "2026-10-13T14:30:00.123+03:00"
}
```

---

### 1.6. Sistem Enum Değerleri (Kesin Eşleşme)
| Enum Adı | İzin Verilen Değerler | Açıklama |
| :--- | :--- | :--- |
| **`Rol`** | `YONETICI`, `PLASIYER` | Kullanıcı rolü. (Admin her şeyi görür, Plasiyer yalnızca kendi portföyünü). |
| **`NumuneDurumu`** | `BEKLEMEDE`, `TEST_ASAMASINDA`, `BEGENDI`, `REDDETTI`, `SIPARISE_DONUSTU` | Numune takip yaşam döngüsü. |
| **`TeklifDurumu`** | `ACIK`, `KAZANILDI`, `KAYBEDILDI`, `IPTAL` | Ticari teklif durumu. |

---

## 2. 17 KOLONLUK EXCEL - API EŞLEŞTİRME MATRİSİ

Kurumsal Excel şablonundaki her kolonun veritabanı ve API karşılığı:

| Kolon | Excel Başlığı | Varlık & Tablo Alanı | API DTO Alanı (Request/Response) |
| :---: | :--- | :--- | :--- |
| **A** | Müşteri / Firma Adı | `companies.name` | `unvan` / `firmaAdi` |
| **B** | Ziyaret Edilen Kişi | `company_contacts.full_name` | `yetkiliAdi` / `yetkiliId` |
| **C** | Görevi / Departmanı | `company_contacts.department_role` | `unvanGorev` / `yetkiliGorevi` |
| **D** | Bölge / Şehir | `companies.city_region` | `sehirBolge` |
| **E** | Ziyaret Tarihi | `visits.visit_date` | `ziyaretTarihi` |
| **F** | Tedarik Ettiği Ürünler | `visits.supplied_products` | `tedarikEttigiUrunler` |
| **G** | Aylık Tüketim Miktarı | `visits.monthly_consumption` | `aylikKullanimMiktari` |
| **H** | Bizden Aldığı Ürünler | `visits.purchased_products` | `bizdenAldigiUrunler` |
| **I** | Mevcut Tedarikçi / Rakip | `visits.current_supplier_competitor` | `mevcutTedarikciRakip` |
| **J** | Ziyaret Konusu | `visits.topic` | `ziyaretKonusu` |
| **K** | Numune Verildi mi? | `visits.has_sample` | `numuneVerildiMi` (`boolean`) |
| **L** | Numune Sonucu / Notu | `samples.result_notes` | `numuneSonucNotu` / `sonucNotlari` |
| **M** | Teklif Verildi mi? | `visits.has_offer` | `teklifVerildiMi` (`boolean`) |
| **N** | Teklif Tutarı | `offers.amount` | `teklifTutari` / `tutar` (`BigDecimal`) |
| **O** | Sonraki Aksiyon | `visits.next_action` | `sonrakiAksiyon` |
| **P** | Sonraki Ziyaret Tarihi | `visits.next_visit_date` | `sonrakiZiyaretTarihi` (Ajanda) |
| **Q** | Notlar | `visits.notes` | `notlar` |

---

## 3. TÜM API KAPILARI DETAYLI REFERANSI

---

### BÖLÜM 1: KİMLİK DOĞRULAMA (AUTHENTICATION)

#### 1.1. Kullanıcı Girişi (Login)
* **Metot & Yol:** `POST /api/v1/kimlik-dogrulama/giris`
* **Güvenlik:** Açık (Public)
* **İstek Gövdesi (`GirisIstegi`):**
```json
{
  "eposta": "admin@akillisaha.com",
  "sifre": "saha1234"
}
```
* **Başarılı Yanıt (200 OK - `KimlikDogrulamaYaniti`):**
```json
{
  "basarili": true,
  "mesaj": "Giriş başarılı",
  "veri": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenTipi": "Bearer",
    "kullaniciId": 1,
    "eposta": "admin@akillisaha.com",
    "adSoyad": "Sistem Yöneticisi",
    "rol": "YONETICI"
  },
  "zamanDamgasi": "2026-10-13T14:30:00.000+03:00"
}
```
* **cURL Örneği:**
```bash
curl -X POST "http://localhost:8080/api/v1/kimlik-dogrulama/giris" \
  -H "Content-Type: application/json" \
  -d '{"eposta":"admin@akillisaha.com","sifre":"saha1234"}'
```

---

#### 1.2. Yeni Kullanıcı Kaydı (Register)
* **Metot & Yol:** `POST /api/v1/kimlik-dogrulama/kayit`
* **Güvenlik:** Açık (Public)
* **İstek Gövdesi (`KayitIstegi`):**
```json
{
  "eposta": "burak.saha@akillisaha.com",
  "sifre": "GucluSifre2026!",
  "adSoyad": "Burak Çelik",
  "rol": "PLASIYER",
  "telefon": "05321112233"
}
```
* **Başarılı Yanıt (201 CREATED - `KimlikDogrulamaYaniti`):** Giriş yanıtı ile aynı yapıda otomatik JWT token döner.

---

#### 1.3. Aktif Kullanıcı Profili
* **Metot & Yol:** `GET /api/v1/kimlik-dogrulama/profilim`
* **Güvenlik:** `Bearer Token` Zorunlu
* **Başarılı Yanıt (200 OK - `KullaniciOzetiYaniti`):**
```json
{
  "basarili": true,
  "mesaj": "İşlem başarıyla tamamlandı",
  "veri": {
    "id": 1,
    "eposta": "admin@akillisaha.com",
    "adSoyad": "Sistem Yöneticisi",
    "rol": "YONETICI",
    "telefon": "05550001122",
    "aktifMi": true,
    "olusturulmaTarihi": "2026-10-12T20:00:00Z"
  },
  "zamanDamgasi": "2026-10-13T14:30:00.000+03:00"
}
```

---

### BÖLÜM 2: MÜŞTERİ FİRMALAR PORTFÖYÜ (COMPANIES)

#### 2.1. Firma Listesi (Filtreli & Sayfalı)
* **Metot & Yol:** `GET /api/v1/firmalar`
* **Güvenlik:** `Bearer Token`
* **Sorgu Parametreleri (Query Params):**
  * `arama` (`string`, opsiyonel): Firma unvanı veya şehir/bölge içinde büyük/küçük harf duyarsız arama.
  * `page` (`int`, varsayılan `0`): Sayfa indeksi.
  * `size` (`int`, varsayılan `20`): Sayfa boyutu.
  * `sort` (`string`, varsayılan `olusturulmaTarihi,desc`): Sıralama.
* **Başarılı Yanıt (200 OK - `Page<FirmaYaniti>`):**
```json
{
  "basarili": true,
  "mesaj": "İşlem başarıyla tamamlandı",
  "veri": {
    "content": [
      {
        "id": 1,
        "unvan": "Ege Gurme Sos A.Ş.",
        "sehirBolge": "İzmir / Bornova",
        "adres": "Sanayi Cad. No:44",
        "telefon": "02324567890",
        "eposta": "info@egegurme.com",
        "mevcutTedarikciRakip": "Rakip Sos Ltd.",
        "tedarikEttigiUrunler": "Nacho Aroması, Hardal",
        "aylikKullanimMiktari": "400 KG",
        "bizdenAldigiUrunler": "Kerry Nacho",
        "atananPlasiyerId": 1,
        "atananPlasiyerAdi": "Sistem Yöneticisi",
        "yetkililer": [
          {
            "id": 1,
            "firmaId": 1,
            "adSoyad": "Ahmet Usta",
            "unvanGorev": "Mutfak Şefi",
            "telefon": "05329998877",
            "eposta": "ahmet@egegurme.com",
            "notlar": "Salı günleri tadım yapıyor",
            "olusturulmaTarihi": "2026-10-12T21:00:00Z"
          }
        ],
        "olusturulmaTarihi": "2026-10-12T20:00:00Z",
        "guncellenmeTarihi": "2026-10-12T20:00:00Z"
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "size": 20,
    "number": 0,
    "empty": false
  },
  "zamanDamgasi": "2026-10-13T14:30:00.000+03:00"
}
```

---

#### 2.2. Yeni Firma Oluştur
* **Metot & Yol:** `POST /api/v1/firmalar`
* **Güvenlik:** `Bearer Token`
* **İstek Gövdesi (`FirmaIstegi`):**
```json
{
  "unvan": "Marmara Unlu Mamulleri Ltd.",
  "sehirBolge": "Bursa / Nilüfer",
  "adres": "Organize Sanayi 2. Cadde No:8",
  "telefon": "02241234567",
  "eposta": "siparis@marmaraunlu.com",
  "mevcutTedarikciRakip": "Rakip Kakao A.Ş.",
  "tedarikEttigiUrunler": "Kakao tozu, vanilin",
  "aylikKullanimMiktari": "1200 KG",
  "bizdenAldigiUrunler": "Henüz yok",
  "atananPlasiyerId": 1
}
```
* **Başarılı Yanıt:** `201 CREATED` (`FirmaYaniti`).

---

#### 2.3. Firma Detayı Getir (360° Kart)
* **Metot & Yol:** `GET /api/v1/firmalar/{id}`
* **Güvenlik:** `Bearer Token`
* **Başarılı Yanıt:** `200 OK` (`FirmaYaniti`).

---

#### 2.4. Firma Güncelle
* **Metot & Yol:** `PUT /api/v1/firmalar/{id}`
* **Güvenlik:** `Bearer Token`
* **İstek Gövdesi:** `FirmaIstegi` (Tüm alanlar güncellenebilir).
* **Başarılı Yanıt:** `200 OK` (`FirmaYaniti`).

---

#### 2.5. Firma Sil
* **Metot & Yol:** `DELETE /api/v1/firmalar/{id}`
* **Güvenlik:** `Bearer Token` (Firmaya bağlı yetkililer ve ziyaretler cascade olarak silinir).
* **Başarılı Yanıt (200 OK):**
```json
{
  "basarili": true,
  "mesaj": "Firma başarıyla silindi",
  "veri": null,
  "zamanDamgasi": "2026-10-13T14:30:00.000+03:00"
}
```

---

#### 2.6. Firmaya Yetkili Kişi Ekle
* **Metot & Yol:** `POST /api/v1/firmalar/{firmaId}/yetkililer`
* **Güvenlik:** `Bearer Token`
* **İstek Gövdesi (`YetkiliIstegi`):**
```json
{
  "firmaId": 1,
  "adSoyad": "Selim Satınalma",
  "unvanGorev": "Satın Alma Direktörü",
  "telefon": "05423334455",
  "eposta": "selim@egegurme.com",
  "notlar": "Fiyat tekliflerinde karar verici"
}
```
* **Başarılı Yanıt:** `201 CREATED` (`YetkiliYaniti`).

---

#### 2.7. Yetkili Kişi Sil
* **Metot & Yol:** `DELETE /api/v1/firmalar/{firmaId}/yetkililer/{yetkiliId}`
* **Güvenlik:** `Bearer Token`
* **Başarılı Yanıt:** `200 OK`.

---

### BÖLÜM 3: SAHA ZİYARETLERİ (VISITS - 30 SANİYELİK FORM)

#### 3.1. 30 Saniyelik Hızlı Ziyaret Kaydet (Tek Adımda Numune & Teklif Bağlama)
> 💡 **En Kritik İş Mantığı Uç Noktası:**  
> Plasiyer tek istek atar; backend `@Transactional` olarak Ziyaret oluşturur.  
> `numuneVerildiMi: true` ise otomatik `samples` tablosuna numune açar.  
> `teklifVerildiMi: true` ise otomatik `offers` tablosuna teklif açar.  
> `sonrakiZiyaretTarihi` doluysa randevu ajandasına düşer.

* **Metot & Yol:** `POST /api/v1/ziyaretler`
* **Güvenlik:** `Bearer Token`
* **İstek Gövdesi (`ZiyaretIstegi`):**
```json
{
  "firmaId": 1,
  "yetkiliId": 1,
  "ziyaretTarihi": "2026-10-13T10:30:00Z",
  "ziyaretKonusu": "Yeni sezon Nacho peynir aroması numune tadımı yapıldı",
  "tedarikEttigiUrunler": "Peynir tozu, baharat karışımları",
  "aylikKullanimMiktari": "500 KG",
  "bizdenAldigiUrunler": "Kerry Nacho Flavour",
  "mevcutTedarikciRakip": "Rakip Aroma Sanayi",
  "numuneVerildiMi": true,
  "numuneUrunAdi": "Nacho Cheese Flavour",
  "numuneMarkasi": "Kerry",
  "numuneMiktari": "1 KG",
  "numuneSonucNotu": "Mutfak şefi lezzeti çok beğendi",
  "teklifVerildiMi": true,
  "teklifBasligi": "2026 Nacho Aroması Yıllık Sözleşme",
  "teklifTutari": 75000.00,
  "teklifParaBirimi": "TRY",
  "sonrakiAksiyon": "Haftaya satın alma ile sözleşme şartları görüşülecek",
  "sonrakiZiyaretTarihi": "2026-10-20T14:00:00Z",
  "notlar": "Rakip firma vadeyi 60 gün veriyormuş, biz 45 güne teklif verdik"
}
```
* **Başarılı Yanıt (201 CREATED - `ZiyaretYaniti`):**
```json
{
  "basarili": true,
  "mesaj": "Ziyaret başarıyla kaydedildi",
  "veri": {
    "id": 1,
    "firmaId": 1,
    "firmaAdi": "Ege Gurme Sos A.Ş.",
    "sehirBolge": "İzmir / Bornova",
    "yetkiliId": 1,
    "yetkiliAdi": "Ahmet Usta",
    "yetkiliGorevi": "Mutfak Şefi",
    "plasiyerId": 1,
    "plasiyerAdi": "Sistem Yöneticisi",
    "ziyaretTarihi": "2026-10-13T10:30:00Z",
    "ziyaretKonusu": "Yeni sezon Nacho peynir aroması numune tadımı yapıldı",
    "tedarikEttigiUrunler": "Peynir tozu, baharat karışımları",
    "aylikKullanimMiktari": "500 KG",
    "bizdenAldigiUrunler": "Kerry Nacho Flavour",
    "mevcutTedarikciRakip": "Rakip Aroma Sanayi",
    "numuneVerildiMi": true,
    "teklifVerildiMi": true,
    "sonrakiAksiyon": "Haftaya satın alma ile sözleşme şartları görüşülecek",
    "sonrakiZiyaretTarihi": "2026-10-20T14:00:00Z",
    "notlar": "Rakip firma vadeyi 60 gün veriyormuş, biz 45 güne teklif verdik",
    "numuneler": [
      {
        "id": 1,
        "ziyaretId": 1,
        "firmaId": 1,
        "firmaAdi": "Ege Gurme Sos A.Ş.",
        "urunAdi": "Nacho Cheese Flavour",
        "marka": "Kerry",
        "miktar": "1 KG",
        "durum": "BEKLEMEDE"
      }
    ],
    "teklifler": [
      {
        "id": 1,
        "ziyaretId": 1,
        "firmaId": 1,
        "firmaAdi": "Ege Gurme Sos A.Ş.",
        "baslik": "2026 Nacho Aroması Yıllık Sözleşme",
        "tutar": 75000.00,
        "paraBirimi": "TRY",
        "durum": "ACIK"
      }
    ],
    "olusturulmaTarihi": "2026-10-13T10:30:05Z"
  },
  "zamanDamgasi": "2026-10-13T14:30:05.000+03:00"
}
```

---

#### 3.2. Ziyaret Listesi
* **Metot & Yol:** `GET /api/v1/ziyaretler`
* **Güvenlik:** `Bearer Token`
* **Sorgu Parametreleri:**
  * `firmaId` (`long`, opsiyonel): Belirli bir firmanın geçmiş ziyaretleri.
  * `page`, `size`, `sort` (`ziyaretTarihi,desc`).
* **Başarılı Yanıt:** `200 OK` (`Page<ZiyaretYaniti>`).

---

#### 3.3. Ajanda & Yaklaşan Randevular (Kolon P)
* **Metot & Yol:** `GET /api/v1/ziyaretler/ajanda`
* **Güvenlik:** `Bearer Token`
* **Sorgu Parametreleri:**
  * `sonTarih` (`ISO-8601 DateTime`, opsiyonel): Filtre bitiş tarihi. Boşsa bugünden itibaren vadesi yaklaşan ve geciken randevuları döner.
* **Başarılı Yanıt (200 OK - `List<ZiyaretYaniti>`):** Ajanda takvim veya liste bileşenine doğrudan basılabilir dizi döner.

---

#### 3.4. Ziyaret Detayı, Güncelleme & Silme
* `GET /api/v1/ziyaretler/{id}` ➔ Detay döner.
* `PUT /api/v1/ziyaretler/{id}` ➔ Ziyaret notu ve aksiyon günceller.
* `DELETE /api/v1/ziyaretler/{id}` ➔ Ziyaret kaydını siler.

---

### BÖLÜM 4: NUMUNE TAKİP MASASI (SAMPLES)

#### 4.1. Numune Listesi (Durum & Firma Filtreli)
* **Metot & Yol:** `GET /api/v1/numuneler`
* **Güvenlik:** `Bearer Token`
* **Sorgu Parametreleri:**
  * `durum` (`NumuneDurumu`, opsiyonel): `BEKLEMEDE`, `TEST_ASAMASINDA`, `BEGENDI`, `REDDETTI`, `SIPARISE_DONUSTU`
  * `firmaId` (`long`, opsiyonel)
  * `page`, `size`, `sort` (`gonderimTarihi,desc`)
* **Başarılı Yanıt:** `200 OK` (`Page<NumuneYaniti>`).

---

#### 4.2. Doğrudan Yeni Numune Kartı Açma
* **Metot & Yol:** `POST /api/v1/numuneler`
* **Güvenlik:** `Bearer Token`
* **İstek Gövdesi (`NumuneIstegi`):**
```json
{
  "firmaId": 1,
  "ziyaretId": null,
  "urunAdi": "DB-82 H Kakao Tozu",
  "marka": "Cargill",
  "miktar": "500 GR",
  "durum": "BEKLEMEDE",
  "sonucNotlari": "Fabrika deneme hattına numune gönderildi"
}
```
* **Başarılı Yanıt:** `201 CREATED` (`NumuneYaniti`).

---

#### 4.3. Numune Durumu Güncelle (Tadım & Sipariş Kapanışı)
* **Metot & Yol:** `PATCH /api/v1/numuneler/{id}/durum`
* **Güvenlik:** `Bearer Token`
* **Sorgu Parametreleri (Query Params):**
  * `durum` (`NumuneDurumu`, Zorunlu): Yeni aşama (Örn: `BEGENDI` veya `SIPARISE_DONUSTU`).
  * `sonucNotlari` (`string`, Opsiyonel): Şefin yorumu veya ret nedeni.
* **cURL Örneği:**
```bash
curl -X PATCH "http://localhost:8080/api/v1/numuneler/1/durum?durum=SIPARISE_DONUSTU&sonucNotlari=Kek%20hatt%C4%B1nda%20denendi%2C%2025%20tonluk%20sipari%C5%9Fe%20d%C3%B6n%C3%BC%C5%9Ft%C3%BC" \
  -H "Authorization: Bearer <TOKEN>"
```
* **Başarılı Yanıt:** `200 OK` (`NumuneYaniti`).

---

#### 4.4. Numune İstatistikleri & Dağılımı
* **Metot & Yol:** `GET /api/v1/numuneler/istatistikler`
* **Güvenlik:** `Bearer Token`
* **Başarılı Yanıt (200 OK):**
```json
{
  "basarili": true,
  "mesaj": "İstatistikler başarıyla getirildi",
  "veri": {
    "TOPLAM": 14,
    "BEKLEMEDE": 4,
    "TEST_ASAMASINDA": 3,
    "BEGENDI": 4,
    "REDDETTI": 1,
    "SIPARISE_DONUSTU": 2,
    "MARKA_Kerry": 9,
    "MARKA_Cargill": 4,
    "MARKA_Orkide": 1
  },
  "zamanDamgasi": "2026-10-13T14:30:00.000+03:00"
}
```

---

#### 4.5. Numune Sil
* **Metot & Yol:** `DELETE /api/v1/numuneler/{id}`
* **Güvenlik:** `Bearer Token`
* **Başarılı Yanıt:** `200 OK`.

---

### BÖLÜM 5: TEKLİF & TİCARİ FIRSATLAR (OFFERS)

#### 5.1. Teklif Listesi
* **Metot & Yol:** `GET /api/v1/teklifler`
* **Güvenlik:** `Bearer Token`
* **Sorgu Parametreleri:**
  * `durum` (`TeklifDurumu`, opsiyonel): `ACIK`, `KAZANILDI`, `KAYBEDILDI`, `IPTAL`
  * `firmaId` (`long`, opsiyonel)
  * `page`, `size`, `sort` (`olusturulmaTarihi,desc`)
* **Başarılı Yanıt:** `200 OK` (`Page<TeklifYaniti>`).

---

#### 5.2. Yeni Fiyat Teklifi Aç
* **Metot & Yol:** `POST /api/v1/teklifler`
* **Güvenlik:** `Bearer Token`
* **İstek Gövdesi (`TeklifIstegi`):**
```json
{
  "firmaId": 1,
  "ziyaretId": null,
  "baslik": "Endüstriyel Bitkisel Yağ Yıllık Tedarik Fırsatı",
  "tutar": 120000.00,
  "paraBirimi": "TRY",
  "durum": "ACIK",
  "gecerlilikTarihi": "2026-11-30T23:59:59Z",
  "notlar": "Ödeme vadesi 45 gün, fabrika teslim"
}
```
* **Başarılı Yanıt:** `201 CREATED` (`TeklifYaniti`).

---

#### 5.3. Teklif Durumu Güncelle (Kazanıldı / Kaybedildi)
* **Metot & Yol:** `PATCH /api/v1/teklifler/{id}/durum`
* **Güvenlik:** `Bearer Token`
* **Sorgu Parametresi:**
  * `durum` (`TeklifDurumu`, Zorunlu): `KAZANILDI`, `KAYBEDILDI`, `IPTAL`, `ACIK`
* **Başarılı Yanıt:** `200 OK` (`TeklifYaniti`).

---

#### 5.4. Teklif Sil
* **Metot & Yol:** `DELETE /api/v1/teklifler/{id}`
* **Güvenlik:** `Bearer Token`
* **Başarılı Yanıt:** `200 OK`.

---

### BÖLÜM 6: 17 KOLONLUK EXCEL MOTORU (EXCEL ENGINE)

#### 6.1. Excel İndir (Export)
* **Metot & Yol:** `GET /api/v1/excel/disa-aktar`
* **Güvenlik:** `Bearer Token`
* **Sorgu Parametreleri:**
  * `plasiyerId` (`long`, opsiyonel): Belirli plasiyere göre filtreleme.
  * `baslangicTarihi` (`ISO-8601`, opsiyonel)
  * `bitisTarihi` (`ISO-8601`, opsiyonel)
* **Dönen İçerik:** Binary Streaming (`application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`).
* **HTTP Header:** `Content-Disposition: attachment; filename="Saha_Ziyaretleri_YYYYMMDD_HHmm.xlsx"`
* **cURL Örneği:**
```bash
curl -X GET "http://localhost:8080/api/v1/excel/disa-aktar" \
  -H "Authorization: Bearer <TOKEN>" \
  -o "Saha_Raporu.xlsx"
```

---

#### 6.2. Excel Dosyası Yükle (Import)
* **Metot & Yol:** `POST /api/v1/excel/ice-aktar`
* **Güvenlik:** `Bearer Token`
* **Content-Type:** `multipart/form-data`
* **Form Parametresi:** `dosya` (`file`, zorunlu - `.xlsx` uzantılı dosya).
* **İş Mantığı:**
  1. Excel dosyasını satır satır okur (Kolon A'dan Q'ya).
  2. Firma mevcut değilse otomatik yeni firma açar.
  3. Yetkili kişi varsa firmaya bağlar.
  4. Ziyaret kaydı açar.
  5. Numune / Teklif bilgisi varsa otomatik oluşturur.
* **Başarılı Yanıt (200 OK - `ExcelIceAktarimSonucuYaniti`):**
```json
{
  "basarili": true,
  "mesaj": "Excel dosyası başarıyla işlendi",
  "veri": {
    "toplamSatirSayisi": 120,
    "yeniFirmaSayisi": 18,
    "olusturulanZiyaretSayisi": 118,
    "olusturulanNumuneSayisi": 24,
    "olusturulanTeklifSayisi": 15,
    "hataMesajlari": [
      "Satır 42: Firma unvanı boş olduğu için atlandı"
    ]
  },
  "zamanDamgasi": "2026-10-13T14:30:00.000+03:00"
}
```

---

### BÖLÜM 7: SİSTEM SAĞLIK DENETİMİ (ACTUATOR & DEVTOOLS)

#### 7.1. Sağlık Kontrolü (Health)
* **Metot & Yol:** `GET /actuator/health`
* **Güvenlik:** Açık (Public)
* **Yanıt:**
```json
{
  "status": "UP"
}
```

---

## 4. 3. KİŞİ VE AI GELİŞTİRİCİ İÇİN KULLANIM ŞABLONLARI

### 4.1. TypeScript Axios İstemcisi Şablonu
3. kişi frontend geliştiricisi aşağıdaki yapılandırmayı doğrudan projesine kopyalayabilir:

```typescript
import axios from 'axios';

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1',
  headers: { 'Content-Type': 'application/json' },
  timeout: 15000,
});

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(err);
  }
);
```

---

### 4.2. n8n Entegrasyonu İçin Hazır HTTP Düğüm Ayarı
n8n iş akışlarında CRM'e otomatik ziyaret veya firma eklemek için:
1. **Düğüm:** `HTTP Request`
2. **Method:** `POST`
3. **URL:** `http://localhost:8080/api/v1/ziyaretler` (veya canlı adres)
4. **Authentication:** `Generic Credential Type` ➔ `Header Auth`
   * **Name:** `Authorization`
   * **Value:** `Bearer <JWT_TOKEN>`
5. **Send Body:** `JSON`
6. **JSON Body:** Bölüm 3.1'deki `ZiyaretIstegi` JSON şablonu.
