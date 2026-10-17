package com.akillisaha.crm.service.impl;

import com.akillisaha.crm.dto.response.TakvimEtkinlikYaniti;
import com.akillisaha.crm.entity.Numune;
import com.akillisaha.crm.entity.Teklif;
import com.akillisaha.crm.entity.Ziyaret;
import com.akillisaha.crm.enums.Rol;
import com.akillisaha.crm.repository.NumuneRepository;
import com.akillisaha.crm.repository.TeklifRepository;
import com.akillisaha.crm.repository.ZiyaretRepository;
import com.akillisaha.crm.security.OzelKullaniciDetaylari;
import com.akillisaha.crm.service.TakvimServisi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Takvim servisinin implementasyonu. Saha ziyaretlerini, ajanda randevularını,
 * numune takiplerini ve teklif geçerlilik tarihlerini tek birleşik akışta derler.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TakvimServisiImpl implements TakvimServisi {

    private final ZiyaretRepository ziyaretRepository;
    private final NumuneRepository numuneRepository;
    private final TeklifRepository teklifRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TakvimEtkinlikYaniti> takvimEtkinlikleriniGetir(
            OzelKullaniciDetaylari aktifKullanici,
            OffsetDateTime baslangic,
            OffsetDateTime bitis,
            String turFiltresi) {

        OffsetDateTime baslangicZamani = baslangic != null ? baslangic : OffsetDateTime.now().minusMonths(1).withDayOfMonth(1).withHour(0).withMinute(0);
        OffsetDateTime bitisZamani = bitis != null ? bitis : OffsetDateTime.now().plusMonths(2).withHour(23).withMinute(59);

        boolean isYonetici = aktifKullanici.getRol() == Rol.YONETICI;
        Long kullaniciId = aktifKullanici.getId();

        List<TakvimEtkinlikYaniti> etkinlikler = new ArrayList<>();

        // 1. Ziyaretler & Planlanan Randevular
        if (turFiltresi == null || turFiltresi.equalsIgnoreCase("ZIYARET") || turFiltresi.equalsIgnoreCase("RANDEVU")) {
            List<Ziyaret> gecmisZiyaretler = isYonetici
                    ? ziyaretRepository.findByZiyaretTarihiBetween(baslangicZamani, bitisZamani)
                    : ziyaretRepository.findByPlasiyerIdAndZiyaretTarihiBetween(kullaniciId, baslangicZamani, bitisZamani);

            for (Ziyaret z : gecmisZiyaretler) {
                if (turFiltresi == null || turFiltresi.equalsIgnoreCase("ZIYARET")) {
                    etkinlikler.add(TakvimEtkinlikYaniti.builder()
                            .id("ZIYARET-" + z.getId())
                            .referansId(z.getId())
                            .tur("ZIYARET")
                            .turEtiketi("Saha Ziyareti")
                            .renkKodu("#3b82f6") // Canlı Mavi
                            .baslik((z.getFirma() != null ? z.getFirma().getUnvan() : "Müşteri") + " - Ziyaret")
                            .tarih(z.getZiyaretTarihi())
                            .firmaId(z.getFirma() != null ? z.getFirma().getId() : null)
                            .firmaUnvani(z.getFirma() != null ? z.getFirma().getUnvan() : "")
                            .sehirBolge(z.getFirma() != null && z.getFirma().getSehirBolge() != null ? z.getFirma().getSehirBolge() : "")
                            .yetkiliAdSoyad(z.getYetkili() != null ? z.getYetkili().getAdSoyad() : "")
                            .yetkiliUnvan(z.getYetkili() != null ? z.getYetkili().getUnvanGorev() : "")
                            .yetkiliTelefon(z.getYetkili() != null && z.getYetkili().getTelefon() != null ? z.getYetkili().getTelefon() : (z.getFirma() != null ? z.getFirma().getTelefon() : ""))
                            .yetkiliEposta(z.getYetkili() != null ? z.getYetkili().getEposta() : "")
                            .plasiyerId(z.getPlasiyer() != null ? z.getPlasiyer().getId() : null)
                            .plasiyerAdSoyad(z.getPlasiyer() != null ? z.getPlasiyer().getAdSoyad() : "")
                            .aciklama(z.getZiyaretKonusu() + (z.getNotlar() != null ? (" | " + z.getNotlar()) : ""))
                            .durum("TAMAMLANDI")
                            .oncelik("NORMAL")
                            .build());
                }
            }

            // Planlanan Sonraki Randevular (Kolon P)
            List<Ziyaret> randevular = isYonetici
                    ? ziyaretRepository.findBySonrakiZiyaretTarihiBetween(baslangicZamani, bitisZamani)
                    : ziyaretRepository.findByPlasiyerIdAndSonrakiZiyaretTarihiBetween(kullaniciId, baslangicZamani, bitisZamani);

            for (Ziyaret z : randevular) {
                if (z.getSonrakiZiyaretTarihi() != null) {
                    boolean vadesiGecti = z.getSonrakiZiyaretTarihi().isBefore(OffsetDateTime.now());
                    etkinlikler.add(TakvimEtkinlikYaniti.builder()
                            .id("RANDEVU-" + z.getId())
                            .referansId(z.getId())
                            .tur("RANDEVU")
                            .turEtiketi("Planlanan Randevu")
                            .renkKodu(vadesiGecti ? "#ef4444" : "#8b5cf6") // Gecikmişse Kırmızı, normalde Mor
                            .baslik((z.getFirma() != null ? z.getFirma().getUnvan() : "Müşteri") + " - Randevu")
                            .tarih(z.getSonrakiZiyaretTarihi())
                            .firmaId(z.getFirma() != null ? z.getFirma().getId() : null)
                            .firmaUnvani(z.getFirma() != null ? z.getFirma().getUnvan() : "")
                            .sehirBolge(z.getFirma() != null && z.getFirma().getSehirBolge() != null ? z.getFirma().getSehirBolge() : "")
                            .yetkiliAdSoyad(z.getYetkili() != null ? z.getYetkili().getAdSoyad() : "")
                            .yetkiliUnvan(z.getYetkili() != null ? z.getYetkili().getUnvanGorev() : "")
                            .yetkiliTelefon(z.getYetkili() != null && z.getYetkili().getTelefon() != null ? z.getYetkili().getTelefon() : (z.getFirma() != null ? z.getFirma().getTelefon() : ""))
                            .yetkiliEposta(z.getYetkili() != null ? z.getYetkili().getEposta() : "")
                            .plasiyerId(z.getPlasiyer() != null ? z.getPlasiyer().getId() : null)
                            .plasiyerAdSoyad(z.getPlasiyer() != null ? z.getPlasiyer().getAdSoyad() : "")
                            .aciklama(z.getSonrakiAksiyon() != null ? z.getSonrakiAksiyon() : "Planlanan sonraki ziyaret randevusu")
                            .durum(vadesiGecti ? "GECIKTI" : "BEKLEMEDE")
                            .oncelik(vadesiGecti ? "YUKSEK" : "NORMAL")
                            .build());
                }
            }
        }

        // 2. Numune Takip Randevuları
        if (turFiltresi == null || turFiltresi.equalsIgnoreCase("NUMUNE")) {
            List<Numune> numuneler = isYonetici
                    ? numuneRepository.findByOlusturulmaTarihiBetween(baslangicZamani, bitisZamani)
                    : numuneRepository.findByPlasiyerIdAndOlusturulmaTarihiBetween(kullaniciId, baslangicZamani, bitisZamani);

            for (Numune n : numuneler) {
                OffsetDateTime tarih = n.getGonderimTarihi() != null ? n.getGonderimTarihi() : n.getOlusturulmaTarihi();
                etkinlikler.add(TakvimEtkinlikYaniti.builder()
                        .id("NUMUNE-" + n.getId())
                        .referansId(n.getId())
                        .tur("NUMUNE")
                        .turEtiketi("Numune Takibi")
                        .renkKodu("#10b981") // Zümrüt Yeşili
                        .baslik((n.getFirma() != null ? n.getFirma().getUnvan() : "") + " (" + n.getUrunAdi() + ")")
                        .tarih(tarih)
                        .firmaId(n.getFirma() != null ? n.getFirma().getId() : null)
                        .firmaUnvani(n.getFirma() != null ? n.getFirma().getUnvan() : "")
                        .sehirBolge(n.getFirma() != null && n.getFirma().getSehirBolge() != null ? n.getFirma().getSehirBolge() : "")
                        .yetkiliAdSoyad("")
                        .yetkiliUnvan("")
                        .yetkiliTelefon(n.getFirma() != null ? n.getFirma().getTelefon() : "")
                        .yetkiliEposta("")
                        .plasiyerId(n.getPlasiyer() != null ? n.getPlasiyer().getId() : null)
                        .plasiyerAdSoyad(n.getPlasiyer() != null ? n.getPlasiyer().getAdSoyad() : "")
                        .aciklama("Ürün: " + n.getUrunAdi() + " | Miktar: " + n.getMiktar() + (n.getSonucNotlari() != null ? (" | Not: " + n.getSonucNotlari()) : ""))
                        .durum(n.getDurum().name())
                        .oncelik("NORMAL")
                        .build());
            }
        }

        // 3. Teklif Geçerlilik Tarihleri
        if (turFiltresi == null || turFiltresi.equalsIgnoreCase("TEKLIF")) {
            List<Teklif> teklifler = isYonetici
                    ? teklifRepository.findByGecerlilikTarihiBetween(baslangicZamani, bitisZamani)
                    : teklifRepository.findByPlasiyerIdAndGecerlilikTarihiBetween(kullaniciId, baslangicZamani, bitisZamani);

            for (Teklif t : teklifler) {
                if (t.getGecerlilikTarihi() != null) {
                    etkinlikler.add(TakvimEtkinlikYaniti.builder()
                            .id("TEKLIF-" + t.getId())
                            .referansId(t.getId())
                            .tur("TEKLIF")
                            .turEtiketi("Teklif Son Gün")
                            .renkKodu("#f59e0b") // Kehribar Sarısı
                            .baslik((t.getFirma() != null ? t.getFirma().getUnvan() : "") + " (" + t.getBaslik() + ")")
                            .tarih(t.getGecerlilikTarihi())
                            .firmaId(t.getFirma() != null ? t.getFirma().getId() : null)
                            .firmaUnvani(t.getFirma() != null ? t.getFirma().getUnvan() : "")
                            .sehirBolge(t.getFirma() != null && t.getFirma().getSehirBolge() != null ? t.getFirma().getSehirBolge() : "")
                            .yetkiliAdSoyad("")
                            .yetkiliUnvan("")
                            .yetkiliTelefon(t.getFirma() != null ? t.getFirma().getTelefon() : "")
                            .yetkiliEposta("")
                            .plasiyerId(t.getPlasiyer() != null ? t.getPlasiyer().getId() : null)
                            .plasiyerAdSoyad(t.getPlasiyer() != null ? t.getPlasiyer().getAdSoyad() : "")
                            .aciklama("Tutar: " + (t.getTutar() != null ? t.getTutar() + " " + t.getParaBirimi() : "Belirtilmedi") + (t.getNotlar() != null ? (" | " + t.getNotlar()) : ""))
                            .durum(t.getDurum().name())
                            .oncelik("YUKSEK")
                            .build());
                }
            }
        }

        // Kronolojik sıralama
        etkinlikler.sort(Comparator.comparing(TakvimEtkinlikYaniti::getTarih, Comparator.nullsLast(Comparator.naturalOrder())));

        return etkinlikler;
    }
}
