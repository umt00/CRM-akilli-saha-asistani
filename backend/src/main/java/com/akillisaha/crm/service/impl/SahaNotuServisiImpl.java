package com.akillisaha.crm.service.impl;

import com.akillisaha.crm.dto.request.SahaNotuIstegi;
import com.akillisaha.crm.dto.response.SahaNotuYaniti;
import com.akillisaha.crm.entity.*;
import com.akillisaha.crm.enums.NumuneDurumu;
import com.akillisaha.crm.enums.Rol;
import com.akillisaha.crm.enums.TeklifDurumu;
import com.akillisaha.crm.repository.*;
import com.akillisaha.crm.service.SahaNotuServisi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * WhatsApp & n8n üzerinden gelen serbest saha metinlerini sıfır maliyetli,
 * kural ve regex tabanlı NLP mantığıyla ayrıştırıp CRM veri tabanına işleyen servis.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SahaNotuServisiImpl implements SahaNotuServisi {

    private final FirmaRepository firmaRepository;
    private final FirmaYetkiliRepository firmaYetkiliRepository;
    private final ZiyaretRepository ziyaretRepository;
    private final NumuneRepository numuneRepository;
    private final TeklifRepository teklifRepository;
    private final UrunRepository urunRepository;
    private final KullaniciRepository kullaniciRepository;

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "a.ş.", "aş", "ltd.", "ltd", "şti.", "şti", "sti", "san.", "san", "tic.", "tic",
            "ve", "ile", "den", "dan", "ten", "tan", "için", "icin", "diye", "olarak"
    ));

    @Override
    @Transactional
    public SahaNotuYaniti sahaNotunuIsle(SahaNotuIstegi istek) {
        String hamMetin = istek.getHamMetin();
        if (hamMetin == null || hamMetin.trim().isEmpty()) {
            return new SahaNotuYaniti(false, "Saha notu metni boş olamaz.");
        }

        // 1. Metni temizle ve tetikleyici ekleri ayıkla (!, #crm, ~crm, &&)
        String temizMetin = temizleMetin(hamMetin);
        log.info("WhatsApp saha notu işleniyor: {}", temizMetin);

        // 2. Plasiyeri (temsilciyi) belirle
        Kullanici plasiyer = plasiyeriBelirle(istek.getPlasiyerId(), istek.getGonderenTel());

        // 3. Firmayı tespit et veya akıllıca aday firma oluştur
        Firma firma = firmayiTespitEt(temizMetin, plasiyer);
        if (firma == null) {
            return new SahaNotuYaniti(false, "Mesajda herhangi bir müşteri firma unvanı tespit edilemedi.");
        }

        // 4. Yetkili kişiyi tespit et (örn: 'ahmet bey', 'mehmet usta')
        FirmaYetkili yetkili = yetkiliyiTespitEt(temizMetin, firma);

        // 5. Ürün tespit et (Katalog veya metin içi)
        Urun urun = urunTespitEt(temizMetin);
        String urunAdi = urun != null ? urun.getAd() : urunAdiCikar(temizMetin);

        // 6. Ziyaret (Müşteri 360° Zaman Çizelgesi Aktivitesi) oluştur
        Ziyaret ziyaret = Ziyaret.builder()
                .firma(firma)
                .yetkili(yetkili)
                .plasiyer(plasiyer)
                .ziyaretTarihi(OffsetDateTime.now())
                .ziyaretKonusu("WhatsApp Saha Bildirimi: " + firma.getUnvan())
                .notlar(hamMetin)
                .tedarikEttigiUrunler(urunAdi)
                .sonrakiAksiyon("WhatsApp saha notu takibi yapılacak.")
                .numuneVerildiMi(false)
                .teklifVerildiMi(false)
                .build();
        ziyaret = ziyaretRepository.save(ziyaret);

        // 7. Numune talebini tespit et ve kaydet
        Numune numune = numuneIsle(temizMetin, firma, plasiyer, ziyaret, urun, urunAdi);
        if (numune != null) {
            ziyaret.setNumuneVerildiMi(true);
        }

        // 8. Teklif / Ticari Fırsat talebini tespit et ve kaydet
        Teklif teklif = teklifIsle(temizMetin, firma, plasiyer, ziyaret, urunAdi);
        if (teklif != null) {
            ziyaret.setTeklifVerildiMi(true);
        }

        // Ziyareti güncelle
        ziyaretRepository.save(ziyaret);

        // 9. WhatsApp teyit mesajı ve operasyonel yanıtı oluştur
        return yanitiHazirla(firma, yetkili, ziyaret, numune, teklif, urunAdi);
    }

    /**
     * Ham mesaj sonundaki tetikleyici sembolleri (!, #crm, ~crm, &&) ve gereksiz boşlukları temizler.
     */
    private String temizleMetin(String hamMetin) {
        String metin = hamMetin.trim();
        // Sondaki tetikleyicileri sil
        metin = metin.replaceAll("(?i)(\\s*(!|#crm|~crm|&&|~y))+\\s*$", "");
        return metin.trim();
    }

    /**
     * Mesajı gönderen plasiyeri bulur veya sistemdeki aktif temsilcilerden birini atar.
     */
    private Kullanici plasiyeriBelirle(Long plasiyerId, String gonderenTel) {
        if (plasiyerId != null) {
            Optional<Kullanici> ops = kullaniciRepository.findById(plasiyerId);
            if (ops.isPresent()) {
                return ops.get();
            }
        }

        // Temsilci rolündeki aktif kullanıcılardan ilkini seç
        List<Kullanici> plasiyerler = kullaniciRepository.findByRol(Rol.PLASIYER);
        if (!plasiyerler.isEmpty()) {
            return plasiyerler.get(0);
        }

        List<Kullanici> tumAktifler = kullaniciRepository.findByAktifMiTrue();
        if (!tumAktifler.isEmpty()) {
            return tumAktifler.get(0);
        }

        return kullaniciRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Sistemde kayıtlı kullanıcı bulunamadı!"));
    }

    /**
     * Metindeki kelimeleri mevcut firmalarla eşleştirir. Eşleşme yoksa metinden firma adını yakalayıp yeni kayıt açar.
     */
    private Firma firmayiTespitEt(String metin, Kullanici plasiyer) {
        String kucukMetin = normalizeMetin(metin);
        List<Firma> tumFirmalar = firmaRepository.findAll();

        Firma enIyiFirma = null;
        int enYuksekSkor = 0;

        for (Firma f : tumFirmalar) {
            String firmaUnvan = normalizeMetin(f.getUnvan());
            int skor = 0;

            // Tam unvan geçiyor mu?
            if (kucukMetin.contains(firmaUnvan)) {
                skor += 50;
            }

            // Kelime bazlı eşleşme
            String[] kelimeler = firmaUnvan.split("\\s+");
            for (String k : kelimeler) {
                if (k.length() > 2 && !STOP_WORDS.contains(k)) {
                    if (kucukMetin.contains(k)) {
                        skor += 10;
                    }
                }
            }

            if (skor > enYuksekSkor) {
                enYuksekSkor = skor;
                enIyiFirma = f;
            }
        }

        if (enYuksekSkor >= 10 && enIyiFirma != null) {
            return enIyiFirma;
        }

        // Mevcut firmalarda bulunamadıysa, metinden unvan yakalamayı dene:
        // Örn: "balci firin a.s den...", "dilek pastanesi şirketinden..."
        Pattern firmaKalip = Pattern.compile("(?i)([a-zçğıöşü0-9\\s]{3,35}?)\\s+(?:a\\.ş\\.|aş|ltd|şti|sti|fırın|firin|pastane|market|restoran|gıda|gida)\\s*(?:'?[dt]en|'?[dt]an)?");
        Matcher matcher = firmaKalip.matcher(kucukMetin);
        if (matcher.find()) {
            String yakalananAd = matcher.group(0).replaceAll("(?i)\\s*('?[dt]en|'?[dt]an)$", "").trim();
            yakalananAd = buyukHarfeCevir(yakalananAd);

            log.info("Yeni aday firma tespit edildi ve CRM'e kaydediliyor: {}", yakalananAd);
            Firma yeniFirma = Firma.builder()
                    .unvan(yakalananAd)
                    .sehirBolge("Saha İletişimi")
                    .atananPlasiyer(plasiyer)
                    .build();
            return firmaRepository.save(yeniFirma);
        }

        return enIyiFirma;
    }

    /**
     * Hitap ekleriyle (bey, hanım, usta, şef, müdür) yetkili ismini yakalar.
     */
    private FirmaYetkili yetkiliyiTespitEt(String metin, Firma firma) {
        Pattern pattern = Pattern.compile("(?i)\\b(?:den|dan|ten|tan)?\\s*([a-zçğıöşü]{3,20}(?:\\s+[a-zçğıöşü]{3,20})?)\\s+(bey|hanım|hanim|usta|şef|sef|müdür|mudur)\\b");
        Matcher matcher = pattern.matcher(metin);

        if (matcher.find()) {
            String isim = matcher.group(1).trim();
            // Prepozisyon ekini temizle
            isim = isim.replaceAll("(?i)^(den|dan|ten|tan)\\s+", "").trim();
            String unvanEki = matcher.group(2).trim();
            String tamAd = buyukHarfeCevir(isim) + " " + buyukHarfeCevir(unvanEki);

            // Firmanın mevcut yetkililerinde var mı?
            List<FirmaYetkili> yetkililer = firmaYetkiliRepository.findByFirmaId(firma.getId());
            for (FirmaYetkili y : yetkililer) {
                if (normalizeMetin(y.getAdSoyad()).contains(normalizeMetin(isim))) {
                    return y;
                }
            }

            // Yeni yetkili olarak kaydet
            FirmaYetkili yeniYetkili = FirmaYetkili.builder()
                    .firma(firma)
                    .adSoyad(tamAd)
                    .unvanGorev("Saha Kontağı (" + buyukHarfeCevir(unvanEki) + ")")
                    .build();
            return firmaYetkiliRepository.save(yeniYetkili);
        }

        return null;
    }

    /**
     * Sistemdeki kayıtlı ürünlerden metinde geçen var mı kontrol eder.
     */
    private Urun urunTespitEt(String metin) {
        String kucukMetin = normalizeMetin(metin);
        List<Urun> urunler = urunRepository.findByAktifMiTrue();

        for (Urun u : urunler) {
            String urunAdi = normalizeMetin(u.getAd());
            if (kucukMetin.contains(urunAdi)) {
                return u;
            }
        }
        return null;
    }

    /**
     * Katalogda olmayan ancak metinde geçen ürün öbeğini yakalar.
     */
    private String urunAdiCikar(String metin) {
        // "orkide zeytin yagi", "nacho cheese", "ayçiçek yağı" gibi ürün tamlamalarını yakalar
        Pattern pattern = Pattern.compile("(?i)\\b([a-zçğıöşü0-9\\s]{3,30}?)\\s*(zeytinyağı|zeytin\\s+yagi|yağ|yag|aroma|peynir|sos|un|unlu\\s+mamul|toz|krema)\\b");
        Matcher matcher = pattern.matcher(metin);
        if (matcher.find()) {
            String hamUrun = matcher.group(0).trim();
            hamUrun = hamUrun.replaceAll("(?i)^[0-9.,\\s]+(adet|koli|teneke|kg|gr)?\\s*", "").trim();
            return buyukHarfeCevir(hamUrun);
        }
        return "Tedarik Ürünü";
    }

    /**
     * Numune anahtar kelimelerini yakalayıp Numune kaydı oluşturur.
     */
    private Numune numuneIsle(String metin, Firma firma, Kullanici plasiyer, Ziyaret ziyaret, Urun urun, String urunAdi) {
        String kucuk = normalizeMetin(metin);
        if (!kucuk.contains("numune") && !kucuk.contains("deneme") && !kucuk.contains("test") && !kucuk.contains("tadim")) {
            return null;
        }

        // Miktar yakalama: "50 teneke numune", "deneme için 50 teneke"
        String miktar = "1 Adet Numune";
        Pattern m1 = Pattern.compile("(?i)(\\d+(?:[.,]\\d+)?)\\s*(teneke|koli|adet|kg|gram|gr|litre|lt|paket|çuval|kutu)\\s+(?:olarak\\s+)?(?:numune|deneme)");
        Matcher matcher1 = m1.matcher(metin);
        if (matcher1.find()) {
            miktar = matcher1.group(1) + " " + matcher1.group(2);
        } else {
            Pattern m2 = Pattern.compile("(?i)(?:numune|deneme)(?:\\s+olarak|\\s+için)?\\s*(\\d+(?:[.,]\\d+)?)\\s*(teneke|koli|adet|kg|gram|gr|litre|lt|paket|çuval|kutu)?");
            Matcher matcher2 = m2.matcher(metin);
            if (matcher2.find()) {
                miktar = matcher2.group(1) + " " + (matcher2.group(2) != null ? matcher2.group(2) : "adet");
            }
        }

        Numune numune = Numune.builder()
                .firma(firma)
                .plasiyer(plasiyer)
                .ziyaret(ziyaret)
                .urun(urun)
                .urunAdi(urunAdi)
                .marka(urun != null ? urun.getMarka() : "Belirtilmedi")
                .miktar(miktar)
                .durum(NumuneDurumu.BEKLEMEDE)
                .gonderimTarihi(OffsetDateTime.now())
                .sonucNotlari("WhatsApp saha notundan otomatik olarak oluşturuldu.")
                .build();

        return numuneRepository.save(numune);
    }

    /**
     * Teklif / Satış fırsatı anahtar kelimelerini yakalayıp Teklif taslağı oluşturur.
     */
    private Teklif teklifIsle(String metin, Firma firma, Kullanici plasiyer, Ziyaret ziyaret, String urunAdi) {
        String kucuk = normalizeMetin(metin);
        boolean teklifIhtiyaci = kucuk.contains("istiyor") || kucuk.contains("siparis") || kucuk.contains("sipariş") ||
                kucuk.contains("teklif") || kucuk.contains("fiyat") || kucuk.contains("talep") || kucuk.contains("anlaşma");

        if (!teklifIhtiyaci) {
            return null;
        }

        // Miktar yakalama: "20.000 adet", "500 koli"
        String miktar = "";
        Pattern miktarKalip = Pattern.compile("(?i)(\\d{1,3}(?:[.,]\\d{3})*|\\d+)\\s*(adet|koli|teneke|kg|ton|paket|kutu)");
        Matcher m = miktarKalip.matcher(metin);
        if (m.find()) {
            miktar = " (" + m.group(0).trim() + ")";
        }

        Teklif teklif = Teklif.builder()
                .firma(firma)
                .ziyaret(ziyaret)
                .plasiyer(plasiyer)
                .baslik(urunAdi + " Tedarik Teklif Talebi" + miktar)
                .durum(TeklifDurumu.ACIK)
                .paraBirimi("TRY")
                .notlar("WhatsApp saha mesajından otomatik teklif taslağı oluşturuldu:\n" + metin)
                .build();

        return teklifRepository.save(teklif);
    }

    /**
     * n8n üzerinden WhatsApp'a iletilecek zengin ve şık teyit mesajını üretir.
     */
    private SahaNotuYaniti yanitiHazirla(Firma firma, FirmaYetkili yetkili, Ziyaret ziyaret,
                                         Numune numune, Teklif teklif, String urunAdi) {
        SahaNotuYaniti yanit = new SahaNotuYaniti();
        yanit.setBasarili(true);
        yanit.setFirmaId(firma.getId());
        yanit.setFirmaUnvani(firma.getUnvan());
        yanit.setZiyaretId(ziyaret.getId());

        if (yetkili != null) {
            yanit.setYetkiliAdi(yetkili.getAdSoyad());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("✅ *").append(firma.getUnvan()).append("* saha notu başarıyla CRM'e işlendi.\n\n");

        if (yetkili != null) {
            sb.append("👤 *Görüşülen:* ").append(yetkili.getAdSoyad()).append("\n");
        }

        if (numune != null) {
            yanit.setNumuneId(numune.getId());
            yanit.setNumuneDetayi(numune.getMiktar() + " " + numune.getUrunAdi());
            sb.append("🧪 *Numune Talebi:* ").append(numune.getMiktar()).append(" ")
              .append(numune.getUrunAdi()).append(" (Durum: Beklemede)\n");
        }

        if (teklif != null) {
            yanit.setTeklifId(teklif.getId());
            yanit.setTeklifDetayi(teklif.getBaslik());
            sb.append("💼 *Ticari Fırsat:* ").append(teklif.getBaslik()).append(" (Durum: Açık Teklif)\n");
        }

        sb.append("📌 *Zaman Çizelgesi:* Not ve detaylar Müşteri 360° kartına eklendi.");
        yanit.setTeyitMesaji(sb.toString());

        Map<String, Object> cikarilan = yanit.getCikarilanBilgiler();
        cikarilan.put("urunAdi", urunAdi);
        if (numune != null) cikarilan.put("numuneMiktar", numune.getMiktar());
        if (teklif != null) cikarilan.put("teklifBaslik", teklif.getBaslik());

        return yanit;
    }

    private String normalizeMetin(String s) {
        if (s == null) return "";
        return s.toLowerCase(Locale.forLanguageTag("tr-TR"))
                .replace("ı", "i")
                .replace("ğ", "g")
                .replace("ü", "u")
                .replace("ş", "s")
                .replace("ö", "o")
                .replace("ç", "c")
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String buyukHarfeCevir(String s) {
        if (s == null || s.isEmpty()) return s;
        String[] parcalar = s.split("\\s+");
        StringBuilder sonuc = new StringBuilder();
        for (String p : parcalar) {
            if (!p.isEmpty()) {
                sonuc.append(Character.toUpperCase(p.charAt(0)));
                if (p.length() > 1) {
                    sonuc.append(p.substring(1).toLowerCase(Locale.forLanguageTag("tr-TR")));
                }
                sonuc.append(" ");
            }
        }
        return sonuc.toString().trim();
    }
}
